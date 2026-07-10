// Configures a ".NET" global tool installation for the dotnet-sdk plugin,
// and forces the download immediately (rather than waiting for a build to
// lazily trigger it) so the SDK is already on disk before objective 3/4
// builds run.
//
// IMPORTANT: this must land at exactly:
//   $JENKINS_HOME/tools/io.jenkins.plugins.dotnet.DotNetSDK/.NET/
// because that's the literal path your Jenkinsfile shells out to:
//   /var/jenkins_home/tools/io.jenkins.plugins.dotnet.DotNetSDK/.NET/dotnet
// The name ".NET" below is what produces that final path segment - if you
// rename it, update the Jenkinsfile's sh step to match.
//
// SDK_URL is pinned to a specific .NET 8 SDK build (project targets net8.0).
// Verify this URL still resolves before relying on it long-term -- Microsoft
// periodically retires old patch builds from the CDN. Current listing:
// https://dotnet.microsoft.com/en-us/download/dotnet/8.0
//
// This groovy script is less battle-tested than the other four in this
// folder -- test it once during authoring and adjust SDK_URL/version if the
// download step fails.
import jenkins.model.*
import io.jenkins.plugins.dotnet.DotNetSDK
import io.jenkins.plugins.dotnet.DotNetSDKInstaller
import hudson.tools.InstallSourceProperty
import hudson.util.StreamTaskListener

def SDK_NAME = ".NET"
def SDK_URL  = "https://builds.dotnet.microsoft.com/dotnet/Sdk/8.0.404/dotnet-sdk-8.0.404-linux-x64.tar.gz"

def instance   = Jenkins.getInstance()
def descriptor = instance.getDescriptorByType(DotNetSDK.DescriptorImpl.class)

def installer = new DotNetSDKInstaller(null)   // null label = applies to any agent
installer.setUrl(SDK_URL)

def installSource = new InstallSourceProperty([installer])
def sdk = new DotNetSDK(SDK_NAME, "", [installSource])
descriptor.setInstallations(sdk)
descriptor.save()

println("Registered .NET SDK tool '" + SDK_NAME + "'. Triggering download now...")
def listener = StreamTaskListener.fromStdout()
def location = installer.performInstallation(sdk, instance, listener)
println("Installed .NET SDK at: " + location)
