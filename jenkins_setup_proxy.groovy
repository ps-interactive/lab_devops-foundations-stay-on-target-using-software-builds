// Configures Jenkins' internal HTTP proxy (used by the Update Center for
// plugin metadata + downloads). Placeholders below are replaced by setup.sh
// at runtime using sed, based on the $https_proxy env var.
import jenkins.model.Jenkins
import hudson.ProxyConfiguration

def j = Jenkins.instance
def proxy = new ProxyConfiguration("proxyAddress", proxyPort as int, "proxyUser", "proxyPassword", "noProxy")
j.proxy = proxy
j.save()
println("Proxy configured")
