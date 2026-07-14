#!/bin/bash
set -e

JENKINS_URL="http://localhost:8082"
JENKINS_CONTAINER="jenkins-docker"
JENKINS_HOME_HOST="/home/pslearner/jenkins-lab"
SETUP_DIR="/home/pslearner/psjenkinslab3a/setup"
CLI_JAR="/tmp/jenkins-cli.jar"

# Clean problematic shebangs from Groovy files, if any got copied in with one
sed -i '/^#!/d' "$SETUP_DIR"/*.groovy

echo "Wait for Jenkins to be fully ready (password file + HTTP + CLI subsystem)"
for i in {1..60}; do
  if sudo test -f "$JENKINS_HOME_HOST/secrets/initialAdminPassword" && \
     curl -fs "$JENKINS_URL/login" >/dev/null 2>&1; then
    echo "Jenkins is responding; allowing extra time for CLI subsystem..."
    sleep 20
    break
  fi
  sleep 5
done

if ! sudo test -f "$JENKINS_HOME_HOST/secrets/initialAdminPassword"; then
    echo "ERROR: Jenkins did not become ready"
    exit 1
fi

echo "Get initial admin password"
INITIAL_ADMIN_PASSWORD=$(sudo cat "$JENKINS_HOME_HOST/secrets/initialAdminPassword")

echo "Download Jenkins CLI"
curl -fsSL -o "$CLI_JAR" "$JENKINS_URL/jnlpJars/jenkins-cli.jar"

echo "Extract proxy values from https_proxy env var"
proxyAddress=$(echo "$https_proxy" | sed -r 's|https?://([^:]+):([^@]+)@([^:]+):([0-9]+)|\3|')
proxyPort=$(echo "$https_proxy" | sed -r 's|https?://([^:]+):([^@]+)@([^:]+):([0-9]+)|\4|')
proxyUser=$(echo "$https_proxy" | sed -r 's|https?://([^:]+):([^@]+)@([^:]+):([0-9]+)|\1|')
proxyPassword=$(echo "$https_proxy" | sed -r 's|https?://([^:]+):([^@]+)@([^:]+):([0-9]+)|\2|')
noProxy='localhost|127.0.0.1|::1'

echo "Update proxy groovy script"
sed -i "s#\"proxyAddress\"#\"$proxyAddress\"#" "$SETUP_DIR/jenkins_setup_proxy.groovy"
sed -i "s#proxyPort#$proxyPort#" "$SETUP_DIR/jenkins_setup_proxy.groovy"
sed -i "s#\"proxyUser\"#\"$proxyUser\"#" "$SETUP_DIR/jenkins_setup_proxy.groovy"
sed -i "s#\"proxyPassword\"#\"$proxyPassword\"#" "$SETUP_DIR/jenkins_setup_proxy.groovy"
sed -i "s#\"noProxy\"#\"$noProxy\"#" "$SETUP_DIR/jenkins_setup_proxy.groovy"

echo "Run Jenkins setup scripts"
java -jar "$CLI_JAR" -s "$JENKINS_URL/" -auth admin:$INITIAL_ADMIN_PASSWORD groovy = < "$SETUP_DIR/jenkins_setup_proxy.groovy"
java -jar "$CLI_JAR" -s "$JENKINS_URL/" -auth admin:$INITIAL_ADMIN_PASSWORD groovy = < "$SETUP_DIR/jenkins_disable_wizard.groovy"
java -jar "$CLI_JAR" -s "$JENKINS_URL/" -auth "admin:$INITIAL_ADMIN_PASSWORD" groovy = < "$SETUP_DIR/jenkins_create_admin_user.groovy"
java -jar "$CLI_JAR" -s "$JENKINS_URL/" -auth "admin:$INITIAL_ADMIN_PASSWORD" groovy = < "$SETUP_DIR/jenkins_install_plugins.groovy"

echo "Restart Jenkins container so newly installed plugins actually load"
sudo docker restart "$JENKINS_CONTAINER"

echo "Wait for Jenkins to come back up after restart"
for i in {1..60}; do
  if curl -fs "$JENKINS_URL/login" >/dev/null 2>&1; then
    sleep 10
    break
  fi
  sleep 5
done

if ! curl -fs "$JENKINS_URL/login" >/dev/null 2>&1; then
    echo "ERROR: Jenkins failed to restart"
    exit 1
fi

echo "Configure and download the .NET SDK global tool (requires dotnet-sdk plugin, installed above, to now be loaded)"
java -jar "$CLI_JAR" -s "$JENKINS_URL/" -auth jenkins:jenkins groovy = < "$SETUP_DIR/jenkins_configure_dotnet_sdk.groovy"

echo "Jenkins setup complete"
