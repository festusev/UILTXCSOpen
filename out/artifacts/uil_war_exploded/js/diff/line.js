"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var base_1 = require("./base");
function generateOptions(options, defaults) {
    if (typeof options === 'function') {
        defaults.callback = options;
    }
    else if (options) {
        for (var name_1 in options) {
            /* istanbul ignore else */
            if (options.hasOwnProperty(name_1)) {
                defaults[name_1] = options[name_1];
            }
        }
    }
    return defaults;
}
exports.lineDiff = new base_1.default();
exports.lineDiff.tokenize = function (value) {
    var retLines = [], linesAndNewlines = value.split(/(\n|\r\n)/);
    // Ignore the final empty token that occurs if the string ends with a new line
    if (!linesAndNewlines[linesAndNewlines.length - 1]) {
        linesAndNewlines.pop();
    }
    // Merge the content and line separators into single tokens
    for (var i = 0; i < linesAndNewlines.length; i++) {
        var line = linesAndNewlines[i];
        if (i % 2 && !this.options.newlineIsToken) {
            retLines[retLines.length - 1] += line;
        }
        else {
            if (this.options.ignoreWhitespace) {
                line = line.trim();
            }
            retLines.push(line);
        }
    }
    return retLines;
};
function diffLines(oldStr, newStr, callback) { return exports.lineDiff.diff(oldStr, newStr, callback); }
exports.diffLines = diffLines;
function diffTrimmedLines(oldStr, newStr, callback) {
    var options = generateOptions(callback, { ignoreWhitespace: true });
    return exports.lineDiff.diff(oldStr, newStr, options);
}
exports.diffTrimmedLines = diffTrimmedLines;
