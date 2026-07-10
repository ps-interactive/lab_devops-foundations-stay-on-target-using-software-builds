// Marks the setup wizard as already completed. Without this, Jenkins keeps
// showing the "Unlock Jenkins" / admin setup screen on every fresh
// JENKINS_HOME, even after an admin account has been created some other way.
import jenkins.model.*
import jenkins.install.*

def instance = Jenkins.getInstance()
instance.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)
instance.save()
println("Setup wizard marked complete")
