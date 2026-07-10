// Installs the plugins this lab actually needs, through the running
// Update Center (requires jenkins_setup_proxy.groovy to have run first,
// so this can reach plugins.jenkins.io through the corporate proxy).
//
//   git                - Source Code Management > Git (objectives 3 & 4)
//   workflow-aggregator- "Pipeline" support (objectives 2 & 4).
//                        NOT bundled in the stock jenkins/jenkins image.
//   dotnet-sdk         - lets Jenkins install/manage a .NET SDK as a
//                        global tool (objective 3's dotnet build step)
import jenkins.model.*

def installed = false
def initialized = false

def pluginslist = ['git', 'workflow-aggregator', 'dotnet-sdk']
def instance = Jenkins.getInstance()
def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()

pluginslist.each {
  println("-------------->>>>> Attempting to install Plugin " + it.trim())
  if (!pm.getPlugin(it)) {
    println("Looking UpdateCenter for " + it)
    if (!initialized) {
      uc.updateAllSites()
      initialized = true
    }
    def plugin = uc.getPlugin(it)
    if (plugin) {
      println("Installing " + it)
      def installFuture = plugin.deploy()
      while (!installFuture.isDone()) {
        println("Waiting for plugin install: " + it)
        sleep(3000)
      }
      installed = true
    } else {
      println("!!!! Could not find plugin in Update Center: " + it)
    }
  } else {
    println(it + " already installed")
  }
}

println("Plugin install pass complete. installed=" + installed)
