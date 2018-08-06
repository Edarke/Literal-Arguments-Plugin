# Literal Argument Code Inspection for Intellij
[![codebeat badge](https://codebeat.co/badges/11b2550c-76af-4d42-a2ae-eb595e7df013)](https://codebeat.co/projects/github-com-edarke-literal-arguments-plugin-master)

This inspection finds literals passed as arguments to methods without comments. It is often not 
clear from context what purpose a literal parameter serves, so Intellij shows parameter hints for
 such cases. However, since most companies do not mandate the use of a specific IDE, style guides
  may require explicit comments for literal arguments. 

This plugin adds a warning for literal parameters passed to a method and 
defines a quick fix to add an appropriate comment. For example, the following method call
```java
fetchCustomer(customerId, true)
```

Can be transformed into:

```java 
fetchCustomer(customerId, /* validate= */ true)
```

To fix all problems in a project or directory, run Analyze > Inspect Code, then right click on
'Potentially confusing code constructs' > 'Literal Argument', select 'Add inline comment for parameter'

Supported Literal Types:
* int
* long
* boolean
* float
* double
* null
* java.util.Optional.empty() and com.google.common.base.Optional.absent()
* Casting one of the above to another type (eg (byte) 0)

Literal chars and Strings are ignored because their purpose is usually evident from their content
. Additionally inspection is disabled for many of the same methods which Intellij's parameter 
hinting is disabled for (eg String.charAt(0) will not require comments)

There is limited support for configuring the format of the generated comment.


## How to install  
Download the jar from releases. Open Intellij, File > Settings > Plugins > Install from disk. 
Restart. 