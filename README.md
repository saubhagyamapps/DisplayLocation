# DisplayLocation 
# Draw Route (Source to Destination)
![ezgif com-resize 2](https://user-images.githubusercontent.com/42333878/44386785-278fe880-a541-11e8-842a-23fbbb53ce58.gif)
#
# create new style google map
# https://mapstyle.withgoogle.com/

 try {

            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        
[style_json.txt](https://github.com/saubhagyamapps/DisplayLocation/files/2313690/style_json.txt)

[uber_map.txt](https://github.com/saubhagyamapps/DisplayLocation/files/2313761/uber_map.txt)

![ezgif com-resize](https://user-images.githubusercontent.com/42333878/44516190-0e726d80-a6e2-11e8-913e-12ebfa4c66ad.png)
![ezgif com-resize 1](https://user-images.githubusercontent.com/42333878/44516638-3adab980-a6e3-11e8-95c0-b86cbccddccd.png)

