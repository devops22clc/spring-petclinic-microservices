pipeline {

    agent none
    options { skipDefaultCheckout() }
    environment {
        OWNER = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
    }
    stages {
        stage('Initialize Variables') {
            agent { label 'controller-node' }
            steps {
                script {
                    def SERVICES = [
                            "spring-petclinic-config-server",
                            "spring-petclinic-discovery-server",
                            "spring-petclinic-api-gateway",
                            "spring-petclinic-customers-service",
                            "spring-petclinic-vets-service",
                            "spring-petclinic-visits-service",
                            "spring-petclinic-admin-server",
                            "spring-petclinic-genai-service"
                    ]
                    env.SERVICES = SERVICES.join(",")

                    switch (env.BRANCH_NAME) {
                    case "main":
                            env.STAGE = "prod"
                            break
                        case "dev":
                            env.STAGE = "dev"
                            break
                        case "uat":
                            env.STAGE = "uat"
                            break
                        default:
                            env.STAGE = env.BRANCH_NAME
                    }
                }
            }
        }
        stage("Detect changes") {
            agent { label 'controller-node' }
            steps {
                dir("${WORKSPACE}"){
                    script {
                        sh(script: "git init && git fetch --no-tags --force --progress -- ${REPO_URL} refs/heads/${BRANCH_NAME}:refs/remotes/origin/${BRANCH_NAME}")
                        def changedFiles = sh(script: "git diff --name-only origin/${BRANCH_NAME}", returnStdout: true).trim().split("\n")
                        def changedServices = [] as Set
                        def rootChanged = false

                        for (file in changedFiles) {
                            echo "file: ${file} || service_as: ${SERVICE_AS}"

                            if (!file.startsWith("${SERVICE_AS}")) {
                                rootChanged = true
                                echo "Changed Root"
                                break
                            } else {
                                def service = file.split("/")[0]
                                changedServices.add(service)
                            }
                        }

                        env.CHANGED_SERVICES = changedServices.join(',')
                        env.IS_CHANGED_ROOT = rootChanged.toString()
                        echo "Changed Services: ${env.CHANGED_SERVICES}"

                        sh "git merge origin/${BRANCH_NAME}"
                    }
                }
            }
        }
        stage("Build & TEST") {
            parallel {
                stage("Build") {
                    //agent { label 'maven-node' }
                    agent any
                    steps {
                        sh "echo run build"
                        checkout scm
                        script {
                            env.GIT_COMMIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            if (env.IS_CHANGED_ROOT == "true")  env.CHANGED_SERVICES = env.SERVICES

                            def changedServices = env.CHANGED_SERVICES.split(',')
                            for (service in changedServices) {
                                sh """
                                cd ${service}
                                echo "run build for ${service}"
                                mvn clean package -DskipTests
                                cd ..
                                """
                            }
                        }
                    }
                     post {
                        always {
                            cleanWs(cleanWhenNotBuilt: false,
                                    deleteDirs: true,
                                    disableDeferredWipeout: true,
                                    notFailBuild: true)
                        }
                    }
                }
                stage("TEST") {
                    //agent { label 'maven-standby-node' }
                    agent any
                    steps {
                        sh "echo run test"
                        checkout scm
                        script {
                            if (env.IS_CHANGED_ROOT == "true")  env.CHANGED_SERVICES = env.SERVICES

                            def changedServices = env.CHANGED_SERVICES.split(',')
                            for (service in changedServices) {
                                echo "Running tests for service: ${service}"
                                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                    sh """
                                    cd ${service}
                                    mvn clean test jacoco:report && mvn clean verify
                                """
                                }
                            }
                        }
                    }
                    post {
                        always {
                            cleanWs(cleanWhenNotBuilt: false,
                                    deleteDirs: true,
                                    disableDeferredWipeout: true,
                                    notFailBuild: true)
                        }
                    }
                }
            }
            post {
                success {
                    node('controller-node') {
                        script {
                            withCredentials([string(credentialsId: 'access-token', variable: 'GITHUB_TOKEN')]) {
                                sh """
                            curl -L \
                            -X POST \
                            -H "Accept: application/vnd.github+json" \
                            -H "Authorization: Bearer ${GITHUB_TOKEN}" \
                            -H "X-GitHub-Api-Version: 2022-11-28" \
                            https://api.github.com/repos/${OWNER}/${REPO_NAME}/statuses/${GIT_COMMIT_SHA} \
                            -d '{"context":"Jenkins-ci", "state":"success","description":"Passed CI"}'
                            """
                            }
                        }
                    }
                }
                failure {
                    node('controller-node') {
                        script {
                            withCredentials([string(credentialsId: 'access-token', variable: 'GITHUB_TOKEN')]) {
                                sh """
                                curl -L \
                                -X POST \
                                -H "Accept: application/vnd.github+json" \
                                -H "Authorization: Bearer ${GITHUB_TOKEN}" \
                                -H "X-GitHub-Api-Version: 2022-11-28" \
                                https://api.github.com/repos/${OWNER}/${REPO_NAME}/statuses/${GIT_COMMIT_SHA} \
                                -d '{"context":"Jenkins-ci", "state":"failure","description":"Failed CI"}'
                                """
                            }
                        }
                    }
                }
            }
        }
        //stage("Push artifact") {
        //    when {
        //        expression { return env.STAGE == "prod" || env.STAGE == "dev" || env.STAGE == "uat" }
        //    }
        //    agent { label 'maven-node' }
        //    steps {
        //        withCredentials([usernamePassword(credentialsId: 'docker-registry-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWD')]) {
        //            sh 'echo "$PASSWD" | docker login --username "$USERNAME" --password-stdin'
        //        }
        //        script {
        //            def changedServices = env.CHANGED_SERVICES.split(',')
        //            for (service in changedServices) {
        //                sh """
        //                    docker push ${OWNER}/${env.STAGE}-${service}:${env.GIT_COMMIT_SHA}
        //                """
        //            }
        //        }
        //        sh "echo y | docker image prune -a && echo y | docker system prune -a"
        //    }
        //}
        //stage("Trigger Github Actions") {
        //
        //}
        //stage('Deploy') {
        //    when {
        //        expression { return env.STAGE == "prod" || env.STAGE == "dev" || env.STAGE == "uat" }
        //    }
        //    agent { label 'kubernetes-node' }
        //    steps {
        //                script {
        //            if (env.IS_CHANGED_ROOT == 'true') {
        //                        echo "Deploying all services"
        //                sh """
        //
        //                """
        //            } else if (env.CHANGED_SERVICES?.trim()) {
        //                def changedServices = env.CHANGED_SERVICES.split(',')
        //                for (service in changedServices) {
        //                    echo "Deploying service: ${service}"
        //                }
        //            }
        //        }
        //    }
        //}
    }
}
