'use strict';

// Required modules
var gulp = require('gulp'),
    path = require('path'),
    _ = require('lodash'),
    doctoc = require('doctoc/lib/transform'),
    del = require('del'),
    $ = require('gulp-load-plugins')(),
    log = require('fancy-log'),
    reactDocsPlugin = require('gulp-react-docs'),
    child_process = require('child_process');

// Helper vars
var docsDest = 'docs';

// Tasks
gulp.task('react-docs', function() {
    var mdTitle = '# React Component Reference';

    return gulp.src('./src/**/*.js')
        .pipe(reactDocsPlugin({
            path: docsDest
        }))
        .pipe($.concat('README.md'))
        .pipe($.tap(function(file) {
            // Generate table of contents for components.md
            var mdWithToc = doctoc(file.contents.toString(), null, 2, mdTitle).data;
            file.contents = new Buffer(mdWithToc);
        }))
        .pipe(gulp.dest(docsDest));
});

gulp.task('default', gulp.series('react-docs'));

gulp.task('clean', function(cb) { del(docsDest, cb) });

gulp.task('check:docs', gulp.series('react-docs', function(cb) {
    child_process.exec('git diff --name-only docs/', function(err, diffFiles) {
        if (diffFiles.indexOf('.md') > -1) {
            log('Automatically generated documentation is not up to \
date with the changes in the codebase. Please run `gulp` and commit the changes.');
            cb(new Error('Docs not up to date!'));
        } else {
            log('Automatically generated documentation is up to date!');
        }
        cb();
    });
}));
