'use strict';

// Required modules
var gulp = require('gulp'),
    path = require('path'),
    _ = require('lodash'),
    doctoc = require('doctoc/lib/transform'),
    del = require('del'),
    $ = require('gulp-load-plugins')(),
    reactDocsPlugin = require('gulp-react-docs');

// Helper vars
var docsDest = 'docs';

// Tasks
gulp.task('default', ['react-docs']);

gulp.task('clean', function(cb) { del(docsDest, cb) });

gulp.task('check:docs', ['docs'], function(cb) {
    exec('git diff --name-only docs/', function(err, diffFiles) {
        if (diffFiles.indexOf('.md') > -1) {
            $.util.log('Automatically generated documentation is not up to \
date with the changes in the codebase. Please run `gulp` and commit the changes.');
            process.exit(1);
        } else {
            $.util.log('Automatically generated documentation is up to date!');
        }
        cb();
    });
});

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
