/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.InputMismatchException;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.services.HashtagService;

/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {

    private final HashtagService hashtagService;

    @Autowired 
    public HashtagSearchController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "hashtags") String hashtags) {
        System.out.println("User is searching: " + hashtags);

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // System.out.println("Hashtag Search: " + hashtags);
        String[] h = parseHashtags(hashtags);
        // System.out.println("Parsed Hashtag Search: " + Arrays.deepToString(h));

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        List<Post> posts = hashtagService.getPostsWithAny(h);
        mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        String errorMessage = error; // previously "Some error occured!" (caused perpetual error message)
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        if (posts.isEmpty())
            mv.addObject("isNoContent", true);

        return mv;
    }
    
    // --- Private Methods ------------------------------------------------------------------------

    /**
     * Parses a single String with potentially multiple hashtags into a String[], where each 
     * element is a String object with a leading "#" and no whitespaces (",", ".", or " ").
     * 
     * @param hashtags
     * @return A String[] of parsed hashtags.
     */
    private String[] parseHashtags(String hashtags) {
        if (hashtags.charAt(0) != '#')
            throw new InputMismatchException("Hashtag does not start with '#'");

        ArrayList<String> parsedHashtags = new ArrayList<>();

        int start = 0;
        int nextHashtag = hashtags.indexOf('#', 1);
        String h;
        while (nextHashtag != -1) {
            h = hashtags.substring(start, nextHashtag);
            h = format(h);
            parsedHashtags.add(h);

            start = nextHashtag;
            nextHashtag = hashtags.indexOf('#', start + 1);
        }
        h = hashtags.substring(start);
        h = format(h);
        parsedHashtags.add(h);

        // System.out.println("\tInner Hashtag-Parse Results: " + parsedHashtags.toString());

        return parsedHashtags.toArray(new String[0]);
    }

    /**
     * Removes the following whitespace: " ", ",", and "."
     * @param hashtag
     * @return the given string without the above listed whitespace.
     */
    private String format(String hashtag) {
        // System.out.print("\t\t Formatting String: " + hashtag + " -> ");
        hashtag = hashtag.replace(" ", "");
        hashtag = hashtag.replace(",", "");
        hashtag = hashtag.replace(".", "");

        // System.out.println(hashtag);
        return hashtag;
    }
}
