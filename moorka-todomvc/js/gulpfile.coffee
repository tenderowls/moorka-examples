gulp = require "gulp"
autoprefixer = require "gulp-autoprefixer"
concat = require "gulp-concat"
rename = require "gulp-rename"

gulp.task "default", ->

  gulp.src "src/**/*.html"
    .pipe gulp.dest "dest"
    
  gulp.src [ "bower_components/**/*.css", "src/**/*.css" ]
    .pipe autoprefixer()
    .pipe concat "app.css"
    .pipe gulp.dest "dest/css"

  gulp.src [ "bower_components/**/*.js", "src/**/*.js" ]
    .pipe concat "app.js"
    .pipe gulp.dest "dest/js"

  gulp.src ["src/**/*.png", "src/**/*.svg", "src/**/*.jpg"] 
    .pipe gulp.dest "dest/images"
        
  gulp.src [ "../target/scala-2.11/moorka-todomvc-fastopt.js" ]
    .pipe rename "./worker.js"
    .pipe gulp.dest "dest/js" 
