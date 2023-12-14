import jenkins.model.Jenkins
import java.util.logging.LogManager

def logger = LogManager.getLogManager().getLogger("")

hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT = true
logger.info("STARTUP SCRIPT - Local Checkouts Allowed")