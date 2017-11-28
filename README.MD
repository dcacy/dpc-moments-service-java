# dpc-moments-service-java

This app will display the moments of a [Watson Workspace](https://workspace.ibm.com) space you indicate.
It's the server-side component of an IBM Engagement Center widget; I had written this in node.js originally,
but for use with the on premises version of IBM Connections and Engagement Center, a Java version makes more sense.
For the client-side code -- and to configure Engagement Center -- 
see the node.js version at [https://github.com/dcacy/dpc-moments-service](https://github.com/dcacy/dpc-moments-service).

Note that with this code deployed on the WebSphere server running Connections, there is no need to configure the Ajax proxy.

Take a look at [this video](https://www.youtube.com/watch?v=6T94Czc3vGk) for an idea of how it works.

Please see the LICENSE file for copyright and license information.

## Getting Started

1. Create a Workspace App at [https://developer.watsonwork.com](https://developer.watsonwork.com). 
Note the App Id and Secret, as you'll need them below.

1. Add the Workspace app to a test space. Note the space Id; you'll need it below.

1. Copy the file `src/main/resources/moments-service-sample.properties` to 
`src/main/resources/moments-service.properties` and modify it to use the App Id and Secret you created above.

1. Download the files `jqcloud.min.css` and `jqcloud.min.js` from 
[http://mistic100.github.io/jQCloud/](http://mistic100.github.io/jQCloud/), 
and copy them to the `src/main/webapp` directory.

1. Edit the file `src/main/webapp/script.js` and use the Id of the test space on line 2. This is for testing.

1. Build the war file with Maven, or use Eclipse with the Maven plugin like I did.

1. Deploy the app to WebSphere or to Liberty, and navigate to its URL in your browser (for example, http://localhost:9080/moments-service/index.html).

## Use

See [this video](https://www.youtube.com/watch?v=6T94Czc3vGk).