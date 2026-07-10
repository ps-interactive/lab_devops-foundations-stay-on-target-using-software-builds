// Creates the learner-facing admin account. Credentials here MUST match
// what projectguide.md tells the learner to type in at step 5:
//   Username: jenkins   Password: jenkins
import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('jenkins', 'jenkins')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()

println("Admin account 'jenkins' created")
