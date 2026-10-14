# CS2114-Project-2
# Compile instructions:

**Requirements:** JDK 25. Maven does not need to be installed; the included Maven
wrapper downloads the right version (3.9.16) on first run, and Maven fetches JavaFX
automatically.

Run these from the project root:

| Task | macOS / Linux | Windows |
|---|---|---|
| Run the app | `./mvnw javafx:run` | `mvnw.cmd javafx:run` |
| Compile | `./mvnw compile` | `mvnw.cmd compile` |
| Run tests | `./mvnw test` | `mvnw.cmd test` |


Supported media: PNG, JPG, BMP and GIF images, and MP4 (H.264) videos.

#  Reflection:
  What changed from your specification? Classes, method signatures, where validation lives, test cases — what did the spec say, what did you build instead, and why?\
  Class: User, SlideshowApp, SlideshowScenes, and PlaylistStorage classes are added.\
  Timer: In the spec, SlideshowPlayer had a timer, but the timer function was moved to each media class. An image ends playing after a set time, and a video ends playing when the video ends.\
  Method: The method draw() in the spec was became getView(), and play(), pause(), and stop() were added to the media class. The method helping skipping forward and backward moved to the Playlist class from the SlideshowPlayer class.\
  What changed in the LinkedChain? Anything you added to or modified in the provided class and why your application needed it, or that you used it unchanged.\
  -> Nothing changed in the LinkedChain.\
  What happened to your unknowns? Of the things you listed in your scope as not knowing how to do, which did you figure out, and which are still open?\
  ->Importing the actual media file is written in the code(GenAI). Tracking metadata is also managed with LinkedChain. For creating java GUI, we used java FX(GenAI).\
  What doesn't work yet? MVP features, bad-input cases, or tests that are missing or failing in this prototype.\
  ->MVP #5 swap is not implemented. \
# Next steps:
  What is left to finish the MVP? The remaining features and fixes, in the order you will do them.\
  ->We should implement swap function. Some of the function such as sort are implemented in the backend but not in Java GUI, so they should be implemented in JavaGUI \
  Which stretch goals will you attempt? The ones from your scope you are committing to, and the ones you are dropping.\
  ->JavaGUI is implemented. Sort function is also implemented but not in GUI, so it should be fixed. Importing the file is implemented. However, exporting the data and adding additional parameters and metadata(color, date, etc) to sort through should be dropped.\
  What new tests will you need? The methods and bad-input cases the added work must cover.\
  ->For implementing GUI, I don't think additional tests are needed. But for the swap function, the new test will be needed. The normal case would be swapping the order of existing files, and the bad case would be rejecting invalid input such as the indicating the file that is out of index.\
  Who does what? Which team member owns each remaining item.\
  ->Sieon would implement swap() method. GUI function will be implemented by Gavin.
# Attribution:
## Classes
| classes | who worked on the actual code |
|------|------|
| Gif | Luis | 
| Image | Luis | 
| Media | Sieon | 
| Video | Luis |  
| SlideshowApp | Luis (w/ GenAI)|  
| SlideshowPlayer | Luis (w/ GenAI) |  
| SlideshowScene | Luis (w/ GenAI)|  
| LinkedChain |  |  
| Playlist | Luis |  
| PlaylistStorage | Luis |  
| User | Luis |  

## Test Classes
| classes | who worked on the actual code |
|------|------|
| FxTestHelper | GenAI | 
| PlaylistStorageTest | Sieon (w/ GenAI) | 
| PlaylistTest | Sieon (w/ GenAI) | 
| StubMedia | GenAI |  
| UserTest | Sieon (w/ GenAI) |  

# GenAI Notice: 
We used GenAI to deal with JavaGUI and importing files. When building the slideshow classes and test classes, we got an assistance from GenAI. We mainly planned with our own logic and let GenAI to review our logic to check whether the overall structure would work. The overall logic was reviewed by GenAI and fixed. 
