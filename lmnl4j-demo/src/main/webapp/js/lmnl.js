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

// ======================================== LmnlLayer

function LmnlLayer() {
}
LmnlLayer.prototype.initLayer = function(owner) {
	this.owner = owner;
	if (this.annotations) {
		for ( var a = 0; a < this.annotations.length; a++) {
			this.annotations[a] = new LmnlAnnotation(this.annotations[a], this);
		}
	}
};
LmnlLayer.prototype.localName = function() {
	return this.name.substring(this.name.indexOf(":") + 1, this.name.length);
};
LmnlLayer.prototype.prefix = function() {
	return this.name.substring(0, this.name.indexOf(":"));
};
LmnlLayer.prototype.contents = function() {
	return this.text ? this.text : this.owner.contents();
}
LmnlLayer.prototype.completeView = function() {
	return new LmnlView(function(a) { return true; }, this);
}

LmnlLayer.prototype.partition = function(annotations) {
	var offsets = [];
	for (var ac = 0; ac < annotations.length; ac++) {
		var range = annotations[ac].range;
		if (range) {
			if (offsets.indexOf(range.start) < 0) offsets.push(range.start);
			if (offsets.indexOf(range.end) < 0) offsets.push(range.end);
		}
	}
	offsets.sort(function(a, b) { return a - b; });

	var contentLength = this.contents().length;
	if (offsets.length == 0 || offsets[0] > 0) offsets.unshift(0);	
	if (offsets.length == 1 || offsets[offsets.length - 1] < contentLength) offsets.push(contentLength);
	
	var segments = [];
	var start = -1;
	for (var oi = 0; oi < offsets.length; oi++) {
		end = offsets[oi];
		if (start >= 0) segments.push(new LmnlRange(start, end));
		start = end;
	}
	return segments;
}

// ======================================== LmnlDocument

function LmnlDocument(model) {
	$.extend(this, model);
	this.initLayer(null);
}
LmnlDocument.prototype = new LmnlLayer;

// ======================================== LmnlAnnotation

function LmnlAnnotation(model, owner) {
	$.extend(this, model);
	this.initLayer(owner);
	if (this.range) {
		this.range = new LmnlRange(this.range[0], this.range[1]);
	}
	if (this.xmlNode) {
		this.xmlNode = new LmnlXmlNodeAddress(this.xmlNode);
	}
}
LmnlAnnotation.prototype = new LmnlLayer;
LmnlAnnotation.prototype.contents = function() {
	var baseText = LmnlLayer.prototype.contents.call(this);
	return this.range ? this.range.of(baseText) : baseText;
}

// ======================================== LmnlRange

function LmnlRange(start, end) {
	this.start = start;
	this.end = end;
};
LmnlRange.prototype.length = function() {
	return this.end - this.start;
};
LmnlRange.prototype.of = function(text) {
	return text.substring(this.start, this.end);
};
LmnlRange.prototype.precedes = function(other) {
	return this.end <= other.start;
};
LmnlRange.prototype.equalsStartOf = function(other) {
	return  (this.start == other.start) && (this.end == other.start);
};

LmnlRange.prototype.overlapsWith = function(other) {
	return this.amountOfOverlapWith(other) > 0;
};

LmnlRange.prototype.amountOfOverlapWith = function(other) {
	return (Math.min(this.end, other.end) - Math.max(this.start, other.start));
};
LmnlRange.prototype.toString = function() {
	return "[" + this.start + ", " + this.end + "]";
};

//======================================== LmnlXmlNodeAddress

function LmnlXmlNodeAddress(model) {
	$.extend(this, model);
};
LmnlXmlNodeAddress.prototype.xpath = function() {
	var xpath = (this.doc ? ("fn:doc('" + this.doc + "')") : "") + "/element()[1]";
	for (var pi = 0; pi < this.pos.length; pi++) {
		xpath += ("/" + (this.pos[pi] == 0 ? "attribute()" : "*[" + this.pos[pi] + "]"));
	}
	return xpath;
};
// ======================================== LmnlView()

function LmnlView(predicate, layer) {
	this.predicate = predicate;
	this.layer = layer;
}
LmnlView.prototype.get = function() {
	return (this.layer.annotations || []).filter(this.predicate);
}
LmnlView.prototype.index = function(view) {
	var partitions = this.layer.partition(this.get());
	var annotations = (view ? view.get() : this.get());
	annotations.sort(function(a, b) { return (a.start == b.start ? b.end - a.end : a.start - b.start); });

	var index = [];

	for ( var pi = 0; pi < partitions.length; pi++) {
		var partition = partitions[pi];
		var overlapping = [];
		for (var ai = 0; ai < annotations.length; ai++) {
			var annotation = annotations[ai];
			var range = annotation.range;
			if (!range) {
				annotations.splice(ai, 1);
				ai--;
				continue;
			}
			
			if (range.overlapsWith(partition) || range.equalsStartOf(partition)) {
				overlapping.push(annotation);
			}
			
//			if (range.precedes(partition)) {
//				annotations.splice(ai, 1);
//				ai--;
//			}
		}
		index.push({ range: partition, annotations: overlapping });
	}
	return index;
}