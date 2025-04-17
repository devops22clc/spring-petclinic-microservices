pipeline {
    agent any
    tools {
        maven 'Maven3.9.6' // Đảm bảo Maven được cấu hình trong Jenkins
        jdk 'OpenJDK-17'        // Đảm bảo JDK 17 được cấu hình trong Jenkins
    }
    environment {
        OWNER = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
        JENKINS_FILE_NAME = "Jenkinsfile"
        DOCKER_CREDENTIALS_ID = 'docker-registry-token' // ID credentials trong Jenkins
    }
    stages {
        stage('Initialize Variables') {
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
                }
            }
        }
        stage("Detect Changes") {
            steps {
                dir("${WORKSPACE}") {
                    script {
                        sh "git fetch origin ${BRANCH_NAME}"
                        def changedFiles = sh(script: "git diff --name-only origin/${BRANCH_NAME}", returnStdout: true).trim().split("\n")
                        def changedServices = [] as Set
                        def rootChanged = false

                        for (file in changedFiles) {
                            if (!file.startsWith("${SERVICE_AS}") && file != "${JENKINS_FILE_NAME}") {
                                rootChanged = true
                                break
                            } else if (file != "${JENKINS_FILE_NAME}") {
                                def service = file.split("/")[0]
                                changedServices.add(service)
                            }
                        }

                        env.CHANGED_SERVICES = changedServices.join(',')
                        env.IS_CHANGED_ROOT = rootChanged.toString()
                        sh "git merge origin/${BRANCH_NAME}"
                    }
                }
            }
        }
        stage("Build & Test") {
            parallel {
                stage("Build and Push Image") {
                    steps {
                        checkout scm
                        script {
                            env.GIT_COMMIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            if (env.IS_CHANGED_ROOT == "true") {
                                env.CHANGED_SERVICES = env.SERVICES
                            }
                            def changedServices = env.CHANGED_SERVICES.split(',')

                            for (service in changedServices) {
                                dir(service) {
                                    sh "mvn clean install -DskipTests -PbuildDocker"
                                }
                            }
                        }
                        withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWD')]) {
                            sh 'echo "$PASSWD" | docker login --username "$USERNAME" --password-stdin'
                        }
                        script {
                            def changedServices = env.CHANGED_SERVICES.split(',')
                            def version = sh(script: 'grep -A 3 "<parent>" pom.xml | grep -oP "<version>\\K[0-9]+\\.[0-9]+\\.[0-9]+"', returnStdout: true).trim()
                            for (service in changedServices) {
                                def imageName = "${OWNER}/${service}:${env.GIT_COMMIT_SHA}"
                                sh "docker tag ${imageName} ${OWNER}/${service}:latest"
                                sh "docker push ${imageName}"
                                if (env.BRANCH_NAME == "main") {
                                    sh "docker push ${OWNER}/${service}:latest"
                                }
                            }
                        }
                        sh "docker image prune -a -f && docker system prune -a -f"
                    }
                    post {
                        always {
                            cleanWs()
                        }
                    }
                }
                stage("Test") {
                    steps {
                        checkout scm
                        script {
                            if (env.IS_CHANGED_ROOT == "true") {
                                env.CHANGED_SERVICES = env.SERVICES
                            }
                            def changedServices = env.CHANGED_SERVICES.split(',')
                            for (service in changedServices) {
                                dir(service) {
                                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                        sh "mvn clean test jacoco:report && mvn verify"
                                    }
                                }
                            }
                        }
                    }
                    post {
                        always {
                            cleanWs()
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        withCredentials([string(credentialsId: 'github-repo-access-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                curl -L \
                                -X POST \
                                -H "Accept: application/vnd.github+json" \
                                -H "Authorization: Bearer ${GITHUB_TOKEN}" \
                                -H "X-GitHub-Api-Version: 2022-11-28" \
                                https://api.github.com/repos/${OWNER}/${REPO_NAME}/statuses/${GIT_COMMIT_SHA} \
                                -d '{"context":"Jenkins-ci", "state":"success","description":"Passed CI", "target_url" : "http://13.250.103.30:8080/job/spring-petclinic-ci-cd/"}'
                            """
                        }
                    }
                }
                failure {
                    script {
                        withCredentials([string(credentialsId: 'github-repo-access-token', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                curl -L \
                                -X POST \
                                -H "Accept: application/vnd.github+json" \
                                -H "Authorization: Bearer ${GITHUB_TOKEN}" \
                                -H "X-GitHub-Api-Version: 2022-11-28" \
                                https://api.github.com/repos/${OWNER}/${REPO_NAME}/statuses/${GIT_COMMIT_SHA} \
                                -d '{"context":"Jenkins-ci", "state":"failure","description":"Failed CI", "target_url" : "http://13.250.103.30:8080/job/spring-petclinic-ci-cd/"}'
                            """
                        }
                    }
                }
            }
        }
    }
}
