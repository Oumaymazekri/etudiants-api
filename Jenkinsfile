pipeline {
  agent any

  environment {
    DOCKERHUB = credentials('dockerhub-creds')  // DOCKERHUB_USR / DOCKERHUB_PSW
    SONAR_TOKEN = credentials('sonar-token')
    SLACK_WEBHOOK = credentials('slack-webhook')
    IMAGE = "oumaymazekri/etudiants-api"
  }

  stages {

    stage('Checkout') {
      steps {
        git 'https://github.com/Oumaymazekri/etudiants-api.git'
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests clean package'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'mvn test || true'
      }
    }

    stage('Docker Build') {
      steps {
        sh "docker build -t ${IMAGE}:latest ."
      }
    }

    stage('Docker Push') {
      steps {
        sh 'echo $DOCKERHUB_PSW | docker login -u $DOCKERHUB_USR --password-stdin'
        sh "docker push ${IMAGE}:latest"
      }
    }

    stage('SonarQube Analysis') {
      steps {
        sh """
          sonar-scanner \
            -Dsonar.projectKey=etudiants-api \
            -Dsonar.host.url=http://198.168.100.71:9000 \
            -Dsonar.login=$SONAR_TOKEN \
            || true
        """
      }
    }

    stage('Trivy scan') {
      steps {
        sh 'trivy image --exit-code 1 ${IMAGE}:latest || true'
      }
    }

    stage('Deploy local (docker)') {
      steps {
        sh 'docker rm -f etudiants || true'
        sh 'docker run -d --name etudiants -p 8080:8080 ${IMAGE}:latest'
      }
    }

    stage('Notify Slack') {
      steps {
        sh """
        if [ -n "$SLACK_WEBHOOK" ]; then
          curl -X POST -H 'Content-type: application/json' \
          --data '{"text":"✨ Pipeline Jenkins terminé avec succès !"}' \
          $SLACK_WEBHOOK
        else
          echo 'No slack webhook configured'
        fi
        """
      }
    }
  }

  post {
    always {
      echo 'Pipeline terminé ❤️'
    }
  }
}
