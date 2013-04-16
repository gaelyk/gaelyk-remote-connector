#Gaelyk Remote Connector

Gaelyk Remote Connector Plugins helps you connecto to your application with your development server.
The typical use case is if you want to run your development server against data stored in your test or production server.
<!--
#Installation

The plugin is distributed using Maven Central as `org.gaelyk:gaelyk-remote-connector:2.0`. 
To install the plugin declare it as `compile` dependency in the Gradle build file.

```
dependencies {
 ...
 compile 'org.gaelyk:gaelyk-remote-connector:2.0'
 ...
}
```
-->
#Usage

To enable connection to the remote application you need to specify your credentials in `gaelyk-remote-connector.properties` file
under `src/main/resources` (assuming you are having maven or gradle source structure).

```
appid=yourgaeappid
username=user@example.com
password=yourpwd
path=/_ah/remote_api
```

You also need to enable remote API in your `web.xml` in application you want to connect to:

```
<servlet>
    <display-name>Remote Api Servlet</display-name>
    <servlet-name>RemoteApiServlet</servlet-name>
    <servlet-class>com.google.apphosting.utils.remoteapi.RemoteApiServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>RemoteApiServlet</servlet-name>
    <url-pattern>/_ah/remote_api</url-pattern>
</servlet-mapping>
```





