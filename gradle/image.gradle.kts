import java.util.*

val imageName =
    project.hasProperty("imageName").let {
        if (it) {
            project.property("imageName") as String
        } else {
            "fewletter/api"
        }
    }

val releaseVersion =
    project.hasProperty("releaseVersion").let {
        if (it) {
            project.property("releaseVersion") as String
        } else {
            Random().nextInt(90000) + 10000
        }
    }

tasks.register("buildDockerImage") {
    group = "image"

    dependsOn("build")

    doLast {
        exec {
            workingDir(".")
            commandLine("docker", "run", "--privileged", "--rm", "tonistiigi/binfmt", "--install", "all")
        }

        exec {
            workingDir(".")
            commandLine("docker", "buildx", "create", "--use")
        }

        exec {
            workingDir(".")
            commandLine(
                "docker",
                "buildx",
                "build",
                "--platform=linux/amd64,linux/arm64",
                "-t",
                "$imageName:latest",
                "--build-arg",
                "RELEASE_VERSION=$releaseVersion",
                ".",
                "--push",
            )
        }

        exec {
            workingDir(".")
            commandLine(
                "docker",
                "buildx",
                "build",
                "--platform=linux/amd64,linux/arm64",
                "-t",
                "$imageName:$releaseVersion",
                "--build-arg",
                "RELEASE_VERSION=$releaseVersion",
                ".",
                "--push",
            )
        }
    }
}

tasks.register("buildEcsDockerImage") {
    group = "image"

    dependsOn("build")

    doLast {
        exec {
            workingDir(".")
            commandLine(
                "docker",
                "build",
                "-t",
                imageName,
                "--build-arg",
                "RELEASE_VERSION=$releaseVersion",
                '.',
            )
        }
    }
}

tasks.register("buildPinpointEcsDockerImageDev") {
    group = "image"

    dependsOn("build")

    doLast {
        exec {
            workingDir(".")
            commandLine(
                "docker",
                "build",
                "-t",
                imageName,
                "--build-arg",
                "RELEASE_VERSION=$releaseVersion",
                "-f",
                "Dockerfile.dev.pinpoint",
                '.',
            )
        }
    }
}

tasks.register("buildPinpointEcsDockerImagePrd") {
    group = "image"

    dependsOn("build")

    doLast {
        exec {
            workingDir(".")
            commandLine(
                "docker",
                "build",
                "-t",
                imageName,
                "--build-arg",
                "RELEASE_VERSION=$releaseVersion",
                "-f",
                "Dockerfile.prd.pinpoint",
                '.',
            )
        }
    }
}