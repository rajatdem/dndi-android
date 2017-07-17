package edu.cmu.msitese.dndiandroid;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import edu.cmu.msitese.dndiandroid.datainference.keyword.KeywordCountDao;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Yu-Lun Tsai on 06/07/2017.
 */

public class KeywordDatabaseTest {

    @Test
    public void testKeywordDaoAddAndClearOperation(){

        KeywordCountDao keywordCountDao = new KeywordCountDao(InstrumentationRegistry.getTargetContext());

        String keyword = "test";
        String category = "hello world";

        keywordCountDao.addOrUpdateKeywordCount(keyword, category);
        keywordCountDao.clearTable();

        int keywordCount = keywordCountDao.getKeywordMatchCount(keyword);
        int categoryCount = keywordCountDao.getKeywordMatchCount(category);
        assertTrue(keywordCount == 0);
        assertTrue(categoryCount == 0);

        keywordCountDao.addOrUpdateKeywordCount(keyword, category);
        keywordCountDao.addOrUpdateKeywordCount(keyword, category);

        keywordCount = keywordCountDao.getKeywordMatchCount(keyword);
        categoryCount = keywordCountDao.getCategoryMatchCount(category);
        assertTrue(keywordCount == 2);
        assertTrue(categoryCount == 2);

        keywordCountDao.clearTable();
    }
}
