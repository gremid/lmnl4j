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

<@lmnl.page title="Beispiel: Dokumentarisches Markup">
	<div class="span-24 last">
		<p>
		<strong>Fahren Sie bitte mit der Maus über den Text!
		Rechts oben wird dann der Annotationskontext für das betreffende Textsegment angezeigt.</strong>
		</p>
		
		<p>Die TEI-P5-Version des gezeigten Dokuments finden Sie <a href="${cp}/example/xml/documentary" title="TEI">hier</a>.</p>
	</div>
	<div id="text" class="span-18"></div>
	<div class="span-6 last">&nbsp;</div>
	<div id="lom" style="border: 1px solid dashed; background: #ff6; font-size: small"></div>
	<script type="text/javascript" src="${cp}/js/example.js"></script>
	<script type="text/javascript">load();</script>
</@lmnl.page>