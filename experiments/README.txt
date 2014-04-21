In order to run the following experiments 
you need to install opencv binding for python (cv2 , not pyopencv).

you can do this by download it from the following link

http://garr.dl.sourceforge.net/project/opencvlibrary/opencv-win/2.4.9/opencv-2.4.9.exe

Than extract cv2.pyd (under build to your site-packages folder).
You also want to ensure that the dlls under (build\x86\vc10\bin) are in your path. 

Pre requirement of cv2 is numpy. which you may find in 

http://www.lfd.uci.edu/~gohlke/pythonlibs/tid72nv9/numpy-MKL-1.8.1.win32-py2.7.exe


Useful tool for re compress videos and convert them from format to format is ffmpeg.

http://ffmpeg.zeranoe.com/builds/win32/static/ffmpeg-latest-win32-static.7z


-------

In order to run the experiments run the following command:

ipython notebook exp.ipynb

