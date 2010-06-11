<#--

    Layered Markup and Annotation Language for Java (lmnl4j):
    implementation of LMNL, a markup language supporting layered and/or
    overlapping annotations.

    Copyright (C) 2010 the respective authors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

-->

<#import "/spring.ftl" as spring>
<#assign xhtmlCompliant = true in spring>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title jsModules=[]>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head profile="http://dublincore.org/documents/dcq-html/">
	<title>${title} :: Layered Markup and Annotation Language (Demo)</title>
	<script type="text/javascript">var ctx = "${cp}"</script>
	<link rel="stylesheet" href="${cp}/css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="${cp}/css/blueprint/plugins/fancy-type/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="${cp}/css/blueprint/print.css" type="text/css" media="print"/>     
        <!--[if lt IE 8]><link rel="stylesheet" href="${cp}/css/blueprint/ie.css" type="text/css" media="screen, projection" /><![endif]-->
	<link rel="stylesheet" type="text/css" href="${cp}/css/lmnl.css" />
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.4.2.js"></script>
	<script type="text/javascript" src="${cp}/js/lmnl.js"></script>
        <#list jsModules as jsModule><script type="text/javascript" src="${cp}/js/${jsModule}.js"></script></#list>
</head>
<body>
	<div class="container">
		<div id="header" class="span-24 last">
			<h1>${title}</h1>
		</div>
		<div id="content" class="span-24 last">
			<#nested>
		</div>
		<div id="footer" class="span-24 last">
			<p>Layered Markup and Annotation Language API. Copyright &copy; 2010 <a href="http://gregor.middell.net/">Gregor Middell</a>.</p>
		</div>
	</div>
</body>
</html>
</#macro>