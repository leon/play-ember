// https://github.com/emberjs/ember.js/blob/master/lib/headless-ember.js

// DOM
var Element = {};
Element.firstChild = function () { return Element; };
Element.innerHTML = function () { return Element; };

var document = { createRange: false, createElement: function() { return Element; } };
var window = this;
this.document = document;

// Console
var console = window.console = {};
console.log = console.info = console.warn = console.error = function(){};

// jQuery
var jQuery = window.jQuery = function() { return jQuery; };
jQuery.ready = function() { return jQuery; };
jQuery.inArray = function() { return jQuery; };
jQuery.jquery = "1.9.0";
jQuery.event = { fixHooks: {} };
var $ = jQuery;

// Ember
function precompile(string) {
    return Ember.Handlebars.precompile(string).toString();
}

function render(string, context) {
    //var template = precompile(string);
    return context.toString();
    //return template(context);
}
