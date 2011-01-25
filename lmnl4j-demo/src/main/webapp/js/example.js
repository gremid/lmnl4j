/*
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var lmnl = null;
var index = null;

function load() {
	$.getJSON(ctx + "/example/document/documentary", function(lom) {
		lmnl = new LmnlDocument(lom);
		index = lmnl.completeView().index();

		var text = $("#text");
		for ( var i = 0; i < index.length; i++) {
			var segment = index[i];
			var span = $("<span/>").attr("id", "s" + i).html(//
					$("<p/>").text(segment.range.of(lmnl.text)).text().replace(/\n/g, "<br/>")//
					);
			text.append(span);
			span.hover(showTagContext(i), removeTagContext(i));
		}
	});
}

function showTagContext(segmentNumber) {
	return function() {
		var segment = index[segmentNumber];
		$("#s" + segmentNumber).css("text-decoration", "underline");
		var context = $("#lom");
		context.hide().empty();
		context.css("position", "fixed").css("right", "50px").css("top", "50px");
		segment.annotations.forEach(function(a) {
			context.append($("<span/>").css("font-weight", "bold").text(a.name));
			context.append($("<br/>"));
			if (a.text) {
				context.append($("<span/>").css("font-style", "italic").text(a.text));
				context.append($("<br/>"));
			}
			if (a.xmlNode) {
				context.append($("<span/>").text(a.xmlNode.xpath()));
				context.append($("<br/>"));
			}
			if (a.annotations) {
				for ( var ac = 0; ac < a.annotations.length; ac++) {
					context.append($("<span/>").css("margin-left", "2em").text(a.annotations[ac].name + "='" + (a.annotations[ac].text || "") + "'"));
					context.append($("<br/>"));
					if (a.annotations[ac].xmlNode) {
						context.append($("<span/>").css("margin-left", "2em").text(a.annotations[ac].xmlNode.xpath()));
						context.append($("<br/>"));
					}
				}
			}
		});
		context.show();
	}
}

function removeTagContext(segmentNumber) {
	return function() {
		$("#lom").hide().empty();
		$("#s" + segmentNumber).css("text-decoration", "none");
	};
}
