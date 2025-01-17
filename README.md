# [Apache F U L C R U M](https://turbine.apache.org/fulcrum/)
--------------------------------------------------------------------------

Fulcrum is a collection of components originally part of the Turbine core
project that are suitable for use in any environment.  They are designed to
be used within any Avalon-compatible container.

## U S A G E 

Apache Fulcrum components might be used standalone, e.g. Fulcrum Crypto, but 
most components work best in an environment which uses the Fulcrum Yaafi service framework 
(which is using the Avalon service lifecycle interfaces aka Avalon container).

You can find a web framework, which is powered by Fulcrum here: [Apache Turbine](https://turbine.apache.org/).

## B U I L D I N G
--------------------------------------------------------------------------
You must have Maven 3.x

Building the Fulcrum from SVN is very easy.  Fulcrum has been
Maven-enabled.  Please refer to the Maven Getting Started document for
instructions on building.  This document is available here:

https://maven.apache.org/guides/getting-started/


### GIT READONLY (may change)
-------------------------------------------

You could use git to checkout current trunk:

    git clone https://github.com/apache/turbine-fulcrum.git
    
    git checkout -b remote-trunk remotes/origin/trunk
    
    
 ### SVN 
 --------------------------------------------------------------------------
 
 Check it out with
 
    svn co https://svn.apache.org/repos/asf/turbine/fulcrum/trunk fulcrum-trunk
    
## Documentation
--------------------------------------------------------------------------

Each component has its section [here](https://turbine.apache.org/fulcrum/).

## Requirements
--------------------------------------------------------------------------

Fulcrum Components requires Java 8. Older components might require Java 7 only.
    
    -----------------------------------------------------------------------
## COMPONENT DEVELOPMENT  
--------------------------------------------------------------------------
### Publishing Workflow

#### Prerequisites

Deploy jars
 
    mvn deploy -Papache-release

More Information:
  - https://www.apache.org/dev/publishing-maven-artifacts.html#prepare-poms
  - http://maven.apache.org/developers/website/deploy-component-reference-documentation.html
  - https://infra.apache.org/publishing-maven-artifacts.html
  
##### Steps

1. Local Testing

    Verify gpg.homedir, gpg.useagent, gpg.passphrase. Check, if -Dgpg.useagent=false is needed,  see below comment to pinentry.
    You may need to add additional profiles, e.g. -Papache-release,java8 or add -Dgpg.passphrase=<xx> 
  
```sh  
mvn clean site install -Papache-release 
```
  
**Multi Module**
 
    mvn release:prepare -DdryRun=true -DautoVersionSubmodules=true -Papache-release
    
Optional security check after mvn clean install:

    mvn org.owasp:dependency-check-maven:aggregate -Ddependency.check.skip=false -DskipTests=true.

**Single**

If dependency check is skipped by default, do mvn org.owasp:dependency-check-maven:check -Ddependency.check.skip=false
Since Turbine Parent 8 security check is enabled by default.

    mvn release:prepare -DdryRun=true -Papache-release 

And finally:

    mvn release:clean    

2. Remote Testing

- May require explicit authentication with -Dusername=<username> -Dpassword=<pw>

**Multi Module**

    mvn release:prepare -DautoVersionSubmodules=true -P apache-release

 
Important: Success will be on the master build, the others are skipped.

**Single**

    mvn release:prepare -Papache-release

Helpful hint from Apache Website: If you're located in Europe then release:prepare may fail with 'Unable to tag SCM' and ' svn: No such revision X '. 
    Wait 10 seconds and run mvn release:prepare again.
  
3. Release Preparing

If you get a 401 error on the upload to repository.apache.org, make sure
that your mvn security settings are in place ~/.m2/settings.xml and ~/.m2/settings-security.xml 

For more information on setting up security see the encryption guide:
 - [GUIDE ENCRYPTION](http://maven.apache.org/guides/mini/guide-encryption.html).

This performs an upload to repository.apache.org/service/local/staging/deploy/maven2/

Hint: Add -Dgpg.useagent=false helps, if running from a windows machine to avoid hanging while gpg plugin signing process 
 .. this may happen, if you do not define the pinentry-program in gpg-agent.conf correctly ..

    mvn release:perform
  
You could find more information here: [Book Reference Staging](http://www.sonatype.com/books/nexus-book/reference/staging.html)
  
4. Close the staging

Login and close in Nexus Repo:

 - [Staging](https://repository.apache.org/index.html#stagingRepositories)
 
 More information available: [CLOSE STAGE](https://www.apache.org/dev/publishing-maven-artifacts.html#close-stage).
 
 Fetch the URL for the tagged Repo from target/checkout with
  
    svn info

  
5. Prepare Voting Information and Voting
 
 ....
  
6. Either Promote / Publish or Drop and Restage

  - [PROMOTE INFO](http://www.apache.org/dev/publishing-maven-artifacts.html#promote)
  - [DROP INFO](http://www.apache.org/dev/publishing-maven-artifacts.html#drop)
  
6a Promote / Release

- Release staged repository in nexus and proceed with next step.
  
6b Revert

- Drop "reverse merge" the release prepare. If backup files (from release:prepare) are still there:

    mvn release:rollback

which will delete the tag in svn repo (since version 3.0.0.-M1, that is does a svn delete ..) and revert to the pre-release state.
Otherwise revert the commits in your checked out workspace and delete the tag manually.

- Drop staged repository in nexus and start again with step 1.


- Don't forget to refer to the failed vote Message-ID in the commit messages (svn, nexus).

 
7. Distribution 

  - (http://www.apache.org/dev/release#upload-ci),
  - http://www.apache.org/dev/release.html#host-GA and 
  - http://www.apache.org/dev/release-publishing.html#distribution
  - SVN checkout target distribution from https://dist.apache.org/repos/dist/release/turbine/<...>/<...>
  - SVN checkout released source from https://svn.apache.org/repos/asf/turbine/<..>/tags/<..>
  - Generate artifacts (check local repo and target for artifacts) from released version:
 
Checkout the tagged released release and run:
 
    mvn clean install package -Papache-release 

 
Generate checksums with UNIX tool shasum, Windows certutil or other tools and 
copy artifacts and sha-files to dist source/binaries folder.

If not all jars are included (assembly plugin _SHOULD_ run after jar generation), run a second time without clean.
If no sha1 files are in the target folder, check local repo.
      
- SVN Add <binaries>, <sources> artifacts (jar/zip/tar.gz,asc,sha1,sha512 files) to target repo
- SVN Remove old releases binaries and sources 

 After repos/dist is updated an automatic email will be generated, if no update of the release database is done:
  
 - [ADD TO TURBINE RELEASE APACHE](https://reporter.apache.org/addrelease.html?turbine)
   

 8.  Stage the latest documentation 

- [DEPLOY DOCU](http://maven.apache.org/developers/website/deploy-component-reference-documentation.html)

SVN Checkout <tagged release version> source. Generate and Publish Site:
IMPORTANT: You may have to clean up the checkoutDirectory of maven-scm-publish-plugin plugin after doing a dry run!

**Multi Module**

    mvn site site:stage scm-publish:publish-scm -Dscmpublish.dryRun=true

**Single**

Omit site:stage, which reqires site element definition in distributionManagement

    mvn site scm-publish:publish-scm -Dscmpublish.dryRun=true
    mvn clean site scm-publish:publish-scm -Dusername=<username> -Dpassword=<pw>


## License

Apache Fulcrum is distributed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
