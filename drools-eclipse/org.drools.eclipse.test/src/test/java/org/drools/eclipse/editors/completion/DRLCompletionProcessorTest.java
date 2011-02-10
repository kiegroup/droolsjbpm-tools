/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.editors.completion;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DRLCompletionProcessorTest {

    @Test
    public void testLookBehind() {
        assertEquals("something", CompletionUtil.stripLastWord(" something"));
        assertEquals("another", CompletionUtil.stripLastWord("another"));

        String s = "rule something \n\nwhen";
        assertEquals("when", CompletionUtil.stripLastWord(s));
    }

    @Test
    public void testPrefixFiltering_FiltersBasedOnDisplayedStringNotContent() {
        List list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "abcd", "zxyz"));
        list.add(new RuleCompletionProposal(0, 0, "azard","good"));
        list.add(new RuleCompletionProposal(0, 0, "art","apple"));
        list.add(new RuleCompletionProposal(0, 0, "spe", "ape"));

        DefaultCompletionProcessor.filterProposalsOnPrefix("a", list);
        assertEquals(3, list.size());
        assertEquals("zxyz", list.get(0).toString());
        assertEquals("good", list.get(1).toString());
        assertEquals("apple", list.get(2).toString());

    }

    @Test
    public void testPrefixFiltering_FiltersAllWhenThereisNoMatches() {
        List list = new ArrayList();
        list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "aardvark", "something"));
        list.add(new RuleCompletionProposal(0, 0, "smeg"));
        list.add(new RuleCompletionProposal(0, 0, "apple"));
        list.add(new RuleCompletionProposal(0, 0, "ape", "zzzzz"));
        DefaultCompletionProcessor.filterProposalsOnPrefix("xzyz", list);
        assertEquals(0, list.size());

    }

    @Test
    public void testPrefixFiltering_IgnoreCase() {
        List list = new ArrayList();
        list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "ART"));
        list.add(new RuleCompletionProposal(0, 0, "art"));
        list.add(new RuleCompletionProposal(0, 0, "aRT"));
        list.add(new RuleCompletionProposal(0, 0, "Art", "zzzzz"));
        DefaultCompletionProcessor.filterProposalsOnPrefix("art", list);
        assertEquals(4, list.size());

        DefaultCompletionProcessor.filterProposalsOnPrefix("ART", list);
        assertEquals(4, list.size());

    }

}
