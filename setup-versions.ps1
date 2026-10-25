# Generates versions/<mc>/ build folders. Edit the table, re-run, then run build-all.ps1.
$ModVersion = "1.0.1"
$Loader = "0.20.1"
$Loom = "1.19-SNAPSHOT"

# mc version to compile against, minecraft range the jar claims, java release,
# remapped (obfuscated MC)?, which src/<hud>/ mixin matches that HUD API
$Versions = @(
	@{ mc = "1.20.1";  range = ">=1.20 <1.20.5";  java = 17; remap = $true;  hud = "gui_1_20"    },
	@{ mc = "1.20.6";  range = ">=1.20.5 <1.21";  java = 21; remap = $true;  hud = "gui_1_20_5"  },
	@{ mc = "1.21.1";  range = ">=1.21 <1.21.6";  java = 21; remap = $true;  hud = "gui_1_21"    },
	@{ mc = "1.21.11"; range = ">=1.21.6 <26";    java = 21; remap = $true;  hud = "gui_1_21"    },
	@{ mc = "26.1.2";  range = "~26.1";           java = 25; remap = $false; hud = "gui_extract" },
	@{ mc = "26.2";    range = "~26.2";           java = 25; remap = $false; hud = "hud_extract" },
	@{ mc = "26.3";    range = "~26.3";           java = 25; remap = $false; hud = "hud_extract" }
)

$root = $PSScriptRoot
# Gradle rejects the BOM that Set-Content adds on Windows PowerShell.
function Write-NoBom($path, $text) { [IO.File]::WriteAllText($path, $text) }
foreach ($v in $Versions) {
	$dir = Join-Path $root "versions\$($v.mc)"
	New-Item -ItemType Directory -Force $dir | Out-Null

	if ($v.remap) {
		$plugin = "net.fabricmc.fabric-loom-remap"
		$deps = @"
	minecraft "com.mojang:minecraft:`${project.minecraft_version}"
	mappings loom.officialMojangMappings()
	modImplementation "net.fabricmc:fabric-loader:`${project.loader_version}"
"@
	} else {
		$plugin = "net.fabricmc.fabric-loom"
		$deps = @"
	minecraft "com.mojang:minecraft:`${project.minecraft_version}"
	implementation "net.fabricmc:fabric-loader:`${project.loader_version}"
"@
	}

	Write-NoBom "$dir\build.gradle" @"
plugins {
	id '$plugin' version "`${loom_version}"
}

apply from: '../../common.gradle'

dependencies {
$deps
}
"@

	Write-NoBom "$dir\settings.gradle" @"
pluginManagement {
	repositories {
		maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }
		mavenCentral()
		gradlePluginPortal()
	}
}
rootProject.name = 'dynamic-crosshair-$($v.mc)'
"@

	Write-NoBom "$dir\gradle.properties" @"
org.gradle.jvmargs=-Xmx2G -Djava.net.preferIPv4Stack=true
minecraft_version=$($v.mc)
mc_range=$($v.range)
java_version=$($v.java)
hud_variant=$($v.hud)
loader_version=$Loader
loom_version=$Loom
mod_version=$ModVersion
"@
}
"Generated $($Versions.Count) version folders."
