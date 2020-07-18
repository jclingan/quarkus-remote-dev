# Quarkus Remote Development with minikube

This project demonstrates how to use remote development using Kubernetes (minikube specifically).

As a developer, you develop locally.
Any changes made to source code,
supporting configuration files, and even the
pom.xml are synchronized with the pod (container) running the
application.
This provides a very (surprisingly!) productive developer experience
with the application running in a more production-like context  -
Kubernetes.

This feature requires no special tooling and minimal configuration.
This example follows the remote development instructions >[here](https://quarkus.io/guides/maven-tooling#remote-development-mode)<.

This example recommends three terminal windows.

## Prerequisites

* Quarkus 1.6.1.Final or later
* minikube 1.11.0 or later (only because that is the version I tested.
May work with earlier versions).
Installation instructions >[here](https://kubernetes.io/docs/tasks/tools/install-minikube/)<
* JDK 11 (Likely to work with JDK 8, but I did not test it)
* Maven 3.6.2+

NOTE: There seems to be a bug in 1.6.0.Final, and 1.6.1.Final is not
available as of this example.
Therefore this example uses the master branch (999-SNAPSHOT).
Even the snapshot contans an error with the remote jar file
not having accesss to a file. It still _seems_ to work :-)

## How this "remote" example differs from local development

*  The POM file adds the following maven dependencies:
   * `quarkus-kubernetes`: Enables Kubernetes deployment and Kubernetes deployment YAML customization using properties in
   _application.properties_.
   * `quarkus-minikube`: Customizes Kubernetes deployment YAML
     for minikube.
    For example,
    automatically adds a NodePort for direct service (Pod) access.
   * `quarkus-container-image-docker`: Generates container image using
     a container registry.
     This example will use the built-in minikube container registry.

* The application.properties file adds the followiing properties:
   * `quarkus.package.type=mutable-jar`: Specifes the generated jar file is mutable-jar.
   This will generate a specialized jar file to support remote development
   * `quarkus.live-reload.password=abc123`: Specifies a remote developmeent password.
   This gives some (perhaps false) sense of security,
   but at least it reduces the odds of a developer conneceting
   to the wrong container.
   The property file is used by both the remote deployment and
   the locally developed app, so the password is used
   by both and the example works out of the box.
   *CHANGE THE PASSWORD!!*.
   * `kubernetes.env-vars[0].name=QUARKUS_LAUNCH_DEVMODE` and 
`kubernetes.env-vars[0].value=true`: Injects an environment variable
into the remote pod that enables remote development mode.
Without this environment variable,
the jar would run as a normal jar file.
This is also an example of customizing the generated YAML.

## Instructions

Use the minikube container registry.

```
eval $(minikube -p minikube docker-env)
```

Deploy the application to Kubernetes. This will start
a Kubernetes pod with the application in remote development
mode.

This may take some time to download container images.

```
mvn \
   clean \
   install \
   -DskipTests \
   -Dquarkus.kubernetes.deploy=true
```

Optionally follow the log output to see output as code updatess are deployed.

```
PODNAME=`kubectl get pods -l app.kubernetes.io/name=devmode -o jsonpath='{range .items[*]}{@.metadata.name}'`

kubectl logs $PODNAME --follow
```

Typically a Quarkus developer would run _mvn quarkus:dev_.
However, for remote development,
we need to invoke a slightly different 
maven goal and point to the pod running the application.

*In a new terminal window*, run the following:

```
NODEPORT=`kubectl get svc devmode -o jsonpath='{.spec.ports[0].nodePort}'`

mvn quarkus:remote-dev -Dquarkus.live-reload.url=http://`minikube ip`:$NODEPORT
```

Open a new terminal window.
Test the endpoint.

```
NODEPORT=`kubectl get svc devmode -o jsonpath='{.spec.ports[0].nodePort}'`

curl http://`minikube ip`:$NODEPORT/hello
```

The output should be _hello_.

Test remote Live Coding.
In src/main/java/org/acme/Hello.java, comment out  `return hello`
and uncomment `return System.getenv("HOSTNAME");`.
Check the endpoint:

```
curl http://`minikube ip`:$NODEPORT/hello
```

The output should be the name of the pod, similar to _devmode-666b57b579-rbdxp_.
