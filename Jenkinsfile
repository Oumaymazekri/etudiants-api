pipeline {
  agent any

  options {
    skipDefaultCheckout(true)
  }

  environment {
    DOCKER_HUB_CRED = credentials('dockerhub-creds')
    SONAR_TOKEN     = credentials('sonar-token')

    IMAGE_NAME     = 'oumaymazekri/etudiants-api'
    IMAGE_TAG      = "v${env.BUILD_NUMBER}"
    APP_PORT       = "8081"          // Port exposé sur l'hôte
    DB_CONTAINER   = "postgres-etudiants"
    APP_CONTAINER  = "etudiants-api"
    NETWORK        = "etudiants-net"
  }

  stages {

    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/Oumaymazekri/etudiants-api.git'
      }
    }

    stage('Setup Docker Network & PostgreSQL') {
      steps {
        script {
          // Créer le réseau si non existant
          sh "docker network inspect ${NETWORK} >/dev/null 2>&1 || docker network create ${NETWORK}"
          
          // Supprimer anciens conteneurs et volumes si existants
          sh "docker rm -f ${DB_CONTAINER} || true"
          sh "docker volume rm ${DB_CONTAINER}-data || true"
          sh "docker volume create ${DB_CONTAINER}-data || true"

          // Lancer PostgreSQL dans le réseau Docker (sans exposer le port sur l'hôte)
          sh """
            docker run -d --name ${DB_CONTAINER} \
              --network ${NETWORK} \
              -v ${DB_CONTAINER}-data:/var/lib/postgresql/data \
              -e POSTGRES_USER=etudiants \
              -e POSTGRES_PASSWORD=etudiants \
              -e POSTGRES_DB=etudiantsdb \
              postgres:15
          """

          // Attendre que PostgreSQL soit prêt
          sh """
            echo "⏳ Waiting for PostgreSQL to become ready..."
            until docker exec ${DB_CONTAINER} pg_isready -U etudiants -d etudiantsdb; do
              sleep 2
            done
            echo "🔥 PostgreSQL is READY!"
          """
        }
      }
    }

    stage('Unit Tests') {
      steps {
        sh """
          mvn -B -Dmaven.test.failure.ignore=false \
            -Dspring.datasource.url=jdbc:postgresql://${DB_CONTAINER}:5432/etudiantsdb \
            -Dspring.datasource.username=etudiants \
            -Dspring.datasource.password=etudiants \
            test
        """
      }
    }

    stage('Build & Docker') {
      steps {
        sh 'mvn -B clean package -DskipTests'
        sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."

        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
          sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
          sh 'docker logout'
        }
      }
    }

    stage('Security Scan (Trivy)') {
      steps {
        script {
          sh 'mkdir -p reports'
          sh """
            trivy fs --severity CRITICAL,HIGH . -f table | tee reports/trivy-source.txt
            trivy image --severity CRITICAL,HIGH ${IMAGE_NAME}:${IMAGE_TAG} -f table | tee reports/trivy-image.txt
          """
        }
        archiveArtifacts artifacts: 'reports/*', fingerprint: true
      }
    }

    stage('Deployment') {
      steps {
        script {
          // Supprimer ancien conteneur si existant
          sh "docker rm -f ${APP_CONTAINER} || true"

          // Lancer le conteneur de l'application
          sh """
            docker run -d --name ${APP_CONTAINER} \
              --network ${NETWORK} \
              -p ${APP_PORT}:8080 \
              -e SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_CONTAINER}:5432/etudiantsdb \
              -e SPRING_DATASOURCE_USERNAME=etudiants \
              -e SPRING_DATASOURCE_PASSWORD=etudiants \
              -e SERVER_PORT=8080 \
              ${IMAGE_NAME}:${IMAGE_TAG}
          """
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('SonarQube') {
          withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_AUTH_TOKEN')]) {
            sh """
              mvn sonar:sonar \
                -Dsonar.projectKey=etudiants-api \
                -Dsonar.host.url=http://198.168.100.71:9000 \
                -Dsonar.login=$SONAR_AUTH_TOKEN
            """
          }
        }
      }
    }
  }

  post {
    success {
      withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK')]) {
        sh """
          curl --fail-with-body -sS -X POST -H 'Content-type: application/json' \
          --data @- "$SLACK" <<'JSON'
          {
            "text": "✅ SUCCESS - Pipeline job ${JOB_NAME} #${BUILD_NUMBER} déployé sur port ${APP_PORT}"
          }
JSON
        """
      }
      echo "🎉 Build OK"
    }

    failure {
      sh "docker logs ${DB_CONTAINER} || true"
      withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK')]) {
        sh """
          curl --fail-with-body -sS -X POST -H 'Content-type: application/json' \
          --data @- "$SLACK" <<'JSON'
          {
            "text": "❌ FAILURE - Pipeline job ${JOB_NAME} #${BUILD_NUMBER}"
          }
JSON
        """
      }
      echo "❌ Build failed"
    }

    always {
      archiveArtifacts artifacts: 'reports/*', allowEmptyArchive: true
    }
  }
}
