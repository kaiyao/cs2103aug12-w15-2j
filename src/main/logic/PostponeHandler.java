package main.logic;

import java.io.IOException;

import java.util.NoSuchElementException;

import main.shared.LogicToUi;
import main.shared.Task;
import main.storage.WillNotWriteToCorruptFileException;


public class PostponeHandler extends CommandHandler {
	private static final String ERROR_INVALID_INDEX = "Invalid index number. Please check!";
	private static final String ERROR_POSTPONE_FLOATING_TASK = "Cannot postpone a floating task. Please check your index.";
	private PostponeParser parser;
	private Task toBePostponed;

	public PostponeHandler(String arguments) {
		super(arguments);
		parser = new PostponeParser(arguments);
	}

	public LogicToUi execute() {
		boolean commandSuccess = false;
		try {
			parser.parse();

			
			// inside parser.
			toBePostponed = parser.getToBePostponed();
		

			String oldTaskDesc = taskToString(toBePostponed);
			String feedbackString = oldTaskDesc + " has been postponed to ";

			pushCurrentTaskListToUndoStack();
			if (toBePostponed.isDeadlineTask()) {
				toBePostponed.changeDeadline(parser.getNewDeadline());
				feedbackString += dateToString(parser.getNewDeadline());
				dataBase.update(toBePostponed.getSerial(), toBePostponed);
				commandSuccess = true;
			} else if (toBePostponed.isTimedTask()) {
				toBePostponed.changeStartAndEndDate(parser.getNewStartTime(), parser.getNewEndTime());
				feedbackString += dateToString(parser.getNewStartTime())+ " to "+ dateToString(parser.getNewEndTime());
				dataBase.update(toBePostponed.getSerial(), toBePostponed);
				commandSuccess = true;
			}
			

			String taskDetails = taskToString(toBePostponed);
			String undoMessage = "postponement of task \"" + taskDetails + "\"";
			pushUndoStatusMessage(undoMessage);
			feedback = new LogicToUi(feedbackString,toBePostponed.getSerial());
		} catch (EmptyDescriptionException e) {
			feedback = new LogicToUi(ERROR_TASKDES_EMPTY);
		} catch (CannotParseDateException e) {
			feedback = new LogicToUi(ERROR_CANNOT_PARSE_DATE);
		} catch (CannotPostponeFloatingException e) {
			feedback = new LogicToUi(ERROR_POSTPONE_FLOATING_TASK);
		} catch (NoSuchElementException e) {
			feedback = new LogicToUi(ERROR_INVALID_INDEX);
		} catch (IOException e) {
			feedback = new LogicToUi(ERROR_IO);
		} catch (WillNotWriteToCorruptFileException e) {
			feedback = new LogicToUi(ERROR_FILE_CORRUPTED);
		} finally {
			if(commandSuccess == false ) {
				popUndoClones();
			}
		}
		return feedback;

	}
}
