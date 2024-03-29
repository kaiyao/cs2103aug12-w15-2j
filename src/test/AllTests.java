//@author A0081007U

package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.ui.UiTests;
import test.logic.LogicTests;
import test.shared.SharedTests;
import test.storage.StorageTests;


@RunWith(Suite.class)
@SuiteClasses({ StorageTests.class, SharedTests.class, LogicTests.class, UiTests.class})

public class AllTests {

}
