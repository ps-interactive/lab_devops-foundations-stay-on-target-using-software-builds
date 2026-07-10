// Optional: removes the proxy config set by jenkins_setup_proxy.groovy.
// Not called by setup.sh by default -- the proxy is internal to the AWS
// training environment and harmless to leave configured, but uncomment the
// call in setup.sh if you'd rather Jenkins show no proxy settings to the
// learner in Manage Jenkins > System.
import jenkins.model.*
import hudson.ProxyConfiguration

def instance = Jenkins.getInstance()
instance.proxy = null
instance.save()
println("Proxy config removed")
