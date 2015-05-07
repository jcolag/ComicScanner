# ComicScanner
A client-side program to examine CBR/CBZ comic book files for identification and validation purposes.

"If you have a few problems, you have trouble, but if you have a whole lot of problems, they start solving each other."  H. Beam Piper, _Ministry of Disturbance_

Over at the [Digital Comic Museum](http://digitalcomicmuseum.com/) and our neighbors [Comic Book Plus](http://comicbookplus.com/)...presumably as well as other archival communities, we often find a handful of problems.

 - The people who scan comics, it being fairly literal back-breaking labor that can take a long time, make the occasional mistake in assembling the archive files.

 - The readers who discover the archives' imperfections can occasionally be...less than gracious, let's say.

 - Updates and improvements to the archives often go unnoticed by the broader reading population.

 - Scans of pages are reused in different archives, from compilations to improved scans.

 - Average readers have trouble discovering whether the archive file they have downloaded is the latest version.

Like Piper's far-future empire, this project is part of an attempt to coax our problems into solving each other.

##Usage

TBD.  Currently, the user interface is no more than is absolutely needed to make sure the code works.  It should be easy to guess the workflow, but assume a real interface design will appear some time in the near future.

##Background

Comic books being primarily graphical and the audience being diverse in terms of technological availability, traditional document formats have been largely worthless.  They are, therefore, almost always distributed as scanned image files (most commonly GIF, JPEG, or PNG) assembled into a [comic book archive](https://en.wikipedia.org/wiki/Comic_book_archive) file, most commonly the RAR compressed format.

These files can be easily unpacked by the reader and examined as individual page images, but dedicated reading applications also exist to display files from the archive sequentially.

It's not a perfect solution, but it works well enough.  Every once in a while, someone has the brilliant idea of designing a new, custom format (or replacing the format with their pet favorite), but this is roundly rejected by the community, if only because the thousands of comic book files would need repackaging.

A few problems, however, arise from the nature of the format itself.

 - The file format carries no systematic metadata.  Some creators add metadata in different ways, but with no agreed-upon standard, it goes largely unused.

 - The majority of metadata comes from the filenames, in many cases, with some users crunching information like publisher, publication date, page count, variants, and so forth in that filename.

 - Since the file creators are often creating both the scan files and the archives, it is up to them to manually name the page images so that every reader application recognizes them in their proper order.

 - Since pages are just image files, they frequently get reused, sometimes without credit, generating ill will in the community.

The means to solve each of these problems comes from solving the others, allowing the problems to solve each other.

##Architecture

On a large scale, this project is half the solution.  It will allow users to upload information on their downloaded comic archives and (more importantly) help the people creating the files to _validate_ them before release.  The other half (not yet created) is a web service that can be searched for a particular comic to provide the missing metadata about the file.

A planned "third half" will be to make the data freely available for download, should anything go wrong with the service.  More on that when I find a good home for the data.

Digging deeper into this project, the core design is a Java applet.  I _know_!  Nobody writes applets anymore.

Yet, comic archive files can sometimes be hundreds of megabytes in size, making it implausibly expensive to validate and process them on a server, due to the required bandwidth (to say nothing of maintaining reliable connections).  A few tests convinced me that the browser Javascript engines aren't yet up to the task of processing large files reliably.  Asking people to install software isn't always successful.  So, even as it continues to decline in popularity, client-side Java (an applet) is the only logical choice for the task.

But it's Java, so if anybody _wants_ to download the program, that's possible, too.

###Applet

The applet:

 - Opens the archive (`ComicScanner::unpackArchive()`).  Currently, ZIP/CBZ and RAR/CBR are supported.  We can investigate TAR/CBT, ACE/CBA, and 7z/CB7 support if there is demand, especially since CBT always seemed like a more appropriate format for comics.  It appears that [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) handles both tar and 7z formats, should that become useful.

 - Identify the file type (`FileInfo::FileInfo()`).  The "magic numbers" are stored in the `signatures` map.  If the file describes an image, additional information gets extracted.  Either way, the extracted metadata is stored in the `fileData` list and its SHA-256 digest stored in `hashes` to identify duplicated files.

 - For archives, iterate through their contents (`FileInfo::extractRar()` and `FileInfo::extractZip()`, presumably with others as more formats are supported, without a common API).  Extract the files into memory individually, to process them through `FileInfo::FileInfo()`, just like the archives, and pull out any additional metadata.

 - Deal with the files in aggregate (`FileInfo::sortFiles()`).  Sort the list by name, find averages of numerical attributes, and (soon) validate names.

 - Report on the files (`ComicScanner::pageReport()`).  Iterate through the files, detecting any conditions worth warning the user about.  __Note__:  This code especially needs work, as each possible warning currently requires its own section of custom code in multiple places to support it.

####Libraries

ComicScanner currently uses the following libraries.

 - [java-unrar](https://code.google.com/p/java-unrar/), available under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

 - [The GNU Crypto](https://www.gnu.org/software/gnu-crypto/) project, used by java-unrar to handle encrypted archives if necessary, available under the [GNU General Public License](https://www.gnu.org/copyleft/gpl.html) with the "library exception."

 - The relevant parts of [Apache HttpComponents](https://hc.apache.org/), specifically the HTTP Client and HTTP Core libraries, and is available under the Apache License 2.0.

 - [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/) is used by the GNU Crypto project and HttpComponents, and is available under the Apache License 2.0.

The code also includes Pierre-Luc Paour's Natural Order Comparator to test the natural sorting of the file names, available under a BSD-like license.

As mentioned, it will be worth exploring [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/).  It does not appear to support the RAR format and ZIP-handling is inbuilt into the Java standard library, but may be an easy win if it reduces the number of maintained code paths, if any of those archive types are genuinely used.

### Web Service

TBD, but expect it to be a straightforward [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) site with a [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) API leveraged by ComicScanner (or anybody else) to upload and download content.

### Data Storage

TBD.

GitHub has a file size limit of 100MB, which sounds like it'd be in the ballpark of information on a couple hundred thousand files.  Golden Age comics run around fifty pages, which would limit the number of comics stored to just a few thousand.  They also have a soft limit of one gigabyte, even limiting the flexibility.  So, that's probably not a great option, even though there's a solid API, and the versioning and merging model is pretty close to perfect if multiple sites need to share data.

Self-hosting will almost certainly happen regardless, but is not a permanent solution, because it limits access to the data to when the site server is operational, making it more difficult for someone to pick up where the project leaves off in an emergency.

Wikidata looks promising in some ways, but seems almost silly (and certainly focused differently) in other ways.  It is absolutely designed to support Wikimedia projects with data, rather than supporting massive tables of arbitrary data.

Bitbucket's free-tier quotas seem to be in the same neighborhood as GitHub, with less clarity over how it can be used.


