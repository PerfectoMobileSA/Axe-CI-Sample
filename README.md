# ADA Automated Testing with AXE Demo

To setup this project:

    1. Replace <<cloud name>> with your perfecto cloud name in WebADA_Axe.java(e.g. demo) or pass it as gradle properties: -PcloudName=<<cloud name>>  
    2. Replace <<security token>> with your perfecto security token in WebADA_Axe.java or pass it as gradle properties: -PsecurityToken=<<SECURITY TOKEN>>
    3. Modify capabilities in WebADA_Axe.java
    4. Update your url & enable or disable preferred tests in testng file

To run this project:
	1.Execute TestNG.xml directly using TestNG if not using gradle -P properties or run gradle test with the required -P properties. E.g:
	
	 gradle clean build test -PcloudName=demo -PsecurityToken=<TOKEN> -PjobName=Axe -PjobNumber=1

Note:</br>
 		To stay updated with Axe-core libs, modify AxeHelper.java ln 28 i.e.   public static final String AXE_DEFAULT = "https://cdnjs.cloudflare.com/ajax/libs/axe-core/3.5.3/axe.min.js";




