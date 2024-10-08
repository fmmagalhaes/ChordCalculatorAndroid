package ChordCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordCalculator {

	private Map<Integer, String> intTonote; // maps 0 to C, 1 to C#, 2 to D, etc

	/**
	 * Constructs a ChordCalculator which allows a set of operations
	 * 
	 * @throws UnknownNoteException
	 */
	public ChordCalculator() {
		intTonote = new HashMap<Integer, String>();
		for (int i = 0; i < Chords.NOTES.length; i++)
			intTonote.put(i, Chords.NOTES[i]);
	}

	/**
	 * @param chord
	 * @param n
	 * @return the result of adding n semitones (half steps) to chord
	 * @throws UnknownNoteException
	 */
	public String addSemitones(String chord, int n) throws UnknownNoteException {
		return adjustSemitones(chord, n, ChordOperation.Addition);
	}

	/**
	 * @param chord
	 * @param n
	 * @return the result of subtracting n semitones (half steps) to chord
	 * @throws UnknownNoteException
	 */
	public String subtractSemitones(String chord, int n) throws UnknownNoteException {
		return adjustSemitones(chord, n, ChordOperation.Subtraction);
	}

	/**
	 * @param notes
	 * @return the chord which is formed exactly by the notes present in the
	 *         parameter
	 * @throws UnknownNoteException
	 */
	public List<String> getChord(List<String> notes) throws UnknownNoteException {
		List<String> notesAux = new ArrayList<String>();

		// need to transform A# to Bb, Db to C#, etc. and remove symbols
		for (String note : notes) {
			notesAux.add(getRootNote(note));
		}

		notes = notesAux;

		ArrayList<String> correctChords = new ArrayList<String>();
		// ArrayList<String> notSoCorrectChords = new ArrayList<String>();
		int length = notes.size();
		int correctChordsCount = 0;
		String bass = getRootNote(notes.get(0));
		for (int i = 0; i < length; i++) {
			String chord = tryRoot(notes, bass);
			System.out.println(chord);
			System.out.println(notes);
			System.out.println(getNotes(chord));
			if (Utils.equalLists(getNotes(chord), notes))
				if (isReasonable(notes, chord))
					correctChords.add(correctChordsCount++, chord);
				else
					correctChords.add(correctChords.size(), chord);
			/*
			 * else if (isReasonable(notes, chord))
			 * notSoCorrectChords.add(chord);
			 */

			// moving first note to last position
			String firstNote = notes.remove(0);
			notes.add(firstNote);
			// next time we'll try a different root
			// resulting in a different inversion
		}
		// return !correctChords.isEmpty() ? correctChords :
		// notSoCorrectChords.subList(0, 1);
		return correctChords;
	}

	/**
	 * @param chord
	 * @return the list of notes which form the chord
	 * @throws UnknownNoteException
	 */
	public List<String> getNotes(String chord) throws UnknownNoteException {
		String[] chordWithBass = chord.split("/");
		String bassNote = "";
		if (chordWithBass.length > 1)
			try {
				bassNote = getRootNote(chordWithBass[chordWithBass.length - 1]);
			} catch (UnknownNoteException e) {
			}

		String root = getRootNote(chord);
		chord = root + getSymbols(chord);
		int n = (Integer) Utils.getKeyFromValue(intTonote, root);

		ArrayList<Integer> compositionSemitones = new ArrayList<Integer>();
		compositionSemitones.add(Chords.ROOT_SEMITONES);

		boolean others = false;
		if (isPowerChord(chord)) {
			compositionSemitones.add(Chords.PERFECT_FIFTH_SEMITONES);
			others = true;
		}

		if (!others) {
			// 3
			if (isSuspendedSecond(chord))
				compositionSemitones.add(Chords.MAJOR_SECOND_SEMITONES);
			else if (isSuspendedFourth(chord))
				compositionSemitones.add(Chords.PERFECT_FOURTH_SEMITONES);
			else if (isMinor(chord) || isDiminished(chord) || isHalfDiminished(chord))
				compositionSemitones.add(Chords.MINOR_THIRD_SEMITONES);
			else // eg major, dominant...
				compositionSemitones.add(Chords.MAJOR_THIRD_SEMITONES);

			// 5
			if (isAugmented(chord) || isAugmentedFifth(chord))
				compositionSemitones.add(Chords.AUGMENTED_FIFTH_SEMITONES);
			else if (isDiminished(chord) || isHalfDiminished(chord) || isJustDiminishedFifth(chord))
				compositionSemitones.add(Chords.DIMINISHED_FIFTH_SEMITONES);
			else
				compositionSemitones.add(Chords.PERFECT_FIFTH_SEMITONES);

			// 6
			if (isAddedSixth(chord)) {
				if (isFlatSixth(chord))
					compositionSemitones.add(Chords.AUGMENTED_FIFTH_SEMITONES);
				else
					compositionSemitones.add(Chords.MAJOR_SIXTH_SEMITONES);
			}

			// 7
			if (isSeventh(chord)
					|| (isNinth(chord) && !isAddedNinth(chord) && !isFlatNinth(chord) && !isSharpNinth(chord))
					|| (isEleventh(chord) && !isAddedEleventh(chord) && !isDiminishedEleventh(chord)
							&& !isSharpEleventh(chord))
					|| (isThirteenth(chord) && !isAddedThirteenth(chord)))
				if (isMajor(chord))
					compositionSemitones.add(Chords.MAJOR_SEVENTH_SEMITONES);
				else if (isDiminished(chord))
					compositionSemitones.add(Chords.DIMINISHED_SEVENTH_SEMITONES);
				else // minor, half diminished, dominant, augmented and others
					compositionSemitones.add(Chords.MINOR_SEVENTH_SEMITONES);

			// 9
			if ((isNinth(chord) || isAddedNinth(chord)) || (isEleventh(chord) && !isAddedEleventh(chord)
					&& !isDiminishedEleventh(chord) && !isSharpEleventh(chord))
					|| (isThirteenth(chord) && !isAddedThirteenth(chord)))
				if (isSharpNinth(chord))
					compositionSemitones.add(Chords.AUGMENTED_NINTH_SEMITONES);
				else if (isFlatNinth(chord))
					compositionSemitones.add(Chords.MINOR_NINTH_SEMITONES);
				else // eg minor major, minor dominant
					compositionSemitones.add(Chords.MAJOR_NINTH_SEMITONES);

			// 11
			if (isEleventh(chord)
					|| (isThirteenth(chord) && !isAddedThirteenth(chord) && !isDiminishedThirteenth(chord)))
				if (isSharpEleventh(chord))
					compositionSemitones.add(Chords.AUGMENTED_ELEVENTH_SEMITONES);
				else if (isDiminished(chord) || isDiminishedEleventh(chord))
					compositionSemitones.add(Chords.DIMINISHED_ELEVENTH_SEMITONES);
				else
					compositionSemitones.add(Chords.PERFECT_ELEVENTH_SEMITONES);

			// 13
			if (isThirteenth(chord))
				if (isDiminishedThirteenth(chord))
					compositionSemitones.add(Chords.MINOR_THIRTEEN_SEMITONES);
				else
					compositionSemitones.add(Chords.MAJOR_THIRTEEN_SEMITONES);

		}

		ArrayList<String> notes = new ArrayList<String>();
		if (!bassNote.isEmpty())
			notes.add(bassNote);
		for (int semitones : compositionSemitones) {
			String note = intTonote.get((n + semitones) % 12);
			if (!notes.contains(note))
				notes.add(note);
		}

		return notes;
	}

	/**
	 ** @param chord
	 ** @return the root of the given chord 
	 */
	public String getRootNote(String chord) throws UnknownNoteException {
		// the return value will be one of the notes in Chords.NOTES
		if (chord.length() > 1)
			chord = chord.substring(0, 2);

		if (!chord.contains("b") && !chord.contains("#"))
			chord = chord.substring(0, 1);

		// check if chord matches any note in NOTES
		for (String note : Chords.NOTES)
			if (chord.equals(note))
				return note;

		// check on alternative notes representation (A# instead of Bb, etc.)
		for (int i = 0; i < Chords.NOTES_ALT.length; i++)
			if (chord.equals(Chords.NOTES_ALT[i]))
				return Chords.NOTES[i];

		throw new UnknownNoteException();
	}

	/**
	 ** @param chord
	 ** @return the symbols of the given chord (ex: M7, 7, m7)
	 */
	public String getSymbols(String chord) {
		String sharpFlat = "";
		if (chord.length() > 1)
			sharpFlat = chord.substring(1, 2);

		if (sharpFlat.equals("b") || sharpFlat.equals("#"))
			chord = chord.substring(2);
		else
			chord = chord.substring(1);

		return chord;
	}

	// checks if a list of notes contains the note equivalent to root+interval
	private boolean containsInterval(List<String> notes, int interval) {
		int m = (Integer) Utils.getKeyFromValue(intTonote, notes.get(0));
		String newNote = intTonote.get((m + interval) % 12);
		return notes.contains(newNote);
	}

	// an inversion is reasonable it is likely to be the correct inversion
	private boolean isReasonable(List<String> notes, String chord) {
		if (!getSymbols(chord).matches("(?s).*(b5|#5|dim).*")
				&& !containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES))
			return false; // Catches most things
		if ((containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
				|| containsInterval(notes, Chords.DIMINISHED_FIFTH_SEMITONES))
				&& containsInterval(notes, Chords.MINOR_THIRD_SEMITONES)
				&& containsInterval(notes, Chords.MINOR_SIXTH_SEMITONES))
			return false; // Catches E, G, B(b), C
		if (chord.contains("6add11"))
			return false; // Catches G, B(b), C, D, E
		return true;
	}

	// based on https://github.com/jsrmath/sharp11
	// trying different roots means trying different inversions
	private String tryRoot(List<String> notes, String bass) throws UnknownNoteException {
		String root = getRootNote(notes.get(0));
		String symbols = "";
		boolean noThird = false;

		// 3
		if (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)) {
			if (containsInterval(notes, Chords.AUGMENTED_FIFTH_SEMITONES)
					&& !(containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
							|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES))) {
				symbols += "+";
			}
		} else if (containsInterval(notes, Chords.MINOR_THIRD_SEMITONES)) {
			if (containsInterval(notes, Chords.DIMINISHED_FIFTH_SEMITONES)
					&& !containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
					&& !containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)) {
				if (containsInterval(notes, Chords.DIMINISHED_SEVENTH_SEMITONES)
						&& !containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES)) {
					symbols += "dim7";
				} else if (containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES)) {
					symbols += "m";
				} else {
					symbols += "dim";
				}
			} else {
				symbols += "m";
			}
		} else {
			if (containsInterval(notes, Chords.PERFECT_FOURTH_SEMITONES))
				symbols += "sus4";
			else if (containsInterval(notes, Chords.MAJOR_SECOND_SEMITONES))
				symbols += "sus2";
			else
				noThird = true;
		}

		// 7
		if (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
				|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES)) {
			if (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)) {
				symbols += "M";
			}
			if (containsInterval(notes, Chords.MAJOR_SIXTH_SEMITONES)) {
				symbols += "13";
			} else if (containsInterval(notes, Chords.PERFECT_FOURTH_SEMITONES)
					&& (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
							|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES))) {
				symbols += "11";
			} else if (containsInterval(notes, Chords.MAJOR_SECOND_SEMITONES)
					&& (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
							|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES))) {
				symbols += "9";
			} else {
				symbols += "7";
			}
		} else if (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES)) {
			if (containsInterval(notes, Chords.MAJOR_SIXTH_SEMITONES)
					&& !containsInterval(notes, Chords.DIMINISHED_FIFTH_SEMITONES)) {
				symbols += "6";
				if (containsInterval(notes, Chords.MAJOR_SECOND_SEMITONES)) {
					symbols += "/9";
				}
			}
			if (containsInterval(notes, Chords.PERFECT_FOURTH_SEMITONES)) {
				symbols += "add11";
			}
			if (!containsInterval(notes, Chords.MAJOR_SIXTH_SEMITONES)
					&& containsInterval(notes, Chords.MAJOR_SECOND_SEMITONES)) {
				symbols += "add9";
			}
		}

		if ((containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES))
				&& containsInterval(notes, Chords.DIMINISHED_FIFTH_SEMITONES)
				&& !containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
				&& (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
						|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES))) {
			symbols += "b5";
		}
		if (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				&& containsInterval(notes, Chords.AUGMENTED_FIFTH_SEMITONES)
				&& !containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
				&& (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
						|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES))) {
			symbols += "#5";
		}
		if (containsInterval(notes, Chords.MINOR_SECOND_SEMITONES)) {
			symbols += "b9";
		}
		if (containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				&& containsInterval(notes, Chords.AUGMENTED_SECOND_SEMITONES)) {
			symbols += "#9";
		}
		if ((containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES))
				&& containsInterval(notes, Chords.DIMINISHED_FIFTH_SEMITONES)
				&& containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
				&& (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
						|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES))) {
			symbols += "#11";
		}
		if ((containsInterval(notes, Chords.MAJOR_THIRD_SEMITONES)
				|| containsInterval(notes, Chords.MINOR_THIRD_SEMITONES))
				&& containsInterval(notes, Chords.MINOR_SIXTH_SEMITONES)
				&& containsInterval(notes, Chords.PERFECT_FIFTH_SEMITONES)
				&& (containsInterval(notes, Chords.MAJOR_SEVENTH_SEMITONES)
						|| containsInterval(notes, Chords.MINOR_SEVENTH_SEMITONES))) {
			symbols += "b13";
		}

		if (noThird) {
			if (symbols.isEmpty()) {
				symbols = "5";
			} else {
				symbols += "no3";
			}
		}

		if (!root.equals(bass))
			symbols += "/" + bass;

		return root + symbols;
	}

	private String adjustSemitones(String chord, int n, ChordOperation operation) throws UnknownNoteException {
		int lastSlashIndex = chord.lastIndexOf('/');
		String bassNote = null;
		if (lastSlashIndex != -1) {
			String possibleBassNote = chord.substring(lastSlashIndex + 1);
			if (isNoteValid(possibleBassNote)) {
				bassNote = possibleBassNote;
				chord = chord.substring(0, lastSlashIndex);
			}
		}

		String symbols = getSymbols(chord);
		String root = getRootNote(chord);
		int m = (Integer) Utils.getKeyFromValue(intTonote, root);

		int newNoteValue = 0;
		switch (operation) {
			case Addition:
				newNoteValue = (m + n) % 12;
				break;
			case Subtraction:
				newNoteValue = (Math.abs(m - n + 12)) % 12;
				break;
		}

		String newRoot = intTonote.get(newNoteValue);
		newRoot += symbols;

		if (bassNote != null) {
			newRoot += "/" + adjustSemitones(bassNote, n, operation);
		}

		return newRoot;
	}

	// removes unknown symbols
	private String cleanChord(String chord) {
		String regex = Utils.arrayToStringRegex(Chords.SYMBOLS, "|");
		String charsToRemove = chord.replaceAll(regex, "");
		for (int i = 0; i < charsToRemove.length(); i++)
			chord = chord.replace(charsToRemove.substring(i, i + 1), "");
		return chord;
	}

	// min maj aug dim
	private boolean isMinor(String chord) {
		return chord.replace("dim", "").replace("dom", "").replace("maj", "").contains("m") || chord.contains("min")
				|| getSymbols(chord).matches("-.*");
	}

	private boolean isMajor(String chord) {
		return chord.contains("M") || chord.contains("maj");
	}

	private boolean isAugmented(String chord) {
		return getSymbols(chord).matches("(\\+|aug).*");
	}

	private boolean isDiminished(String chord) {
		chord = chord.replace("dom", "");
		return !isHalfDiminished(chord)
				&& (chord.contains("o") || chord.contains("�") || getSymbols(chord).matches("dim.*"));
	}

	private boolean isHalfDiminished(String chord) {
		return chord.contains("�") || chord.contains("�") || chord.contains("half")
				|| (chord.contains("om7") && !chord.contains("dom7"));
	}

	// suspended chords
	private boolean isSuspendedSecond(String chord) {
		return chord.contains("sus2");
	}

	private boolean isSuspendedFourth(String chord) {
		return chord.contains("sus4") || (!isSuspendedSecond(chord) && chord.contains("sus"));
	}

	// added tone chords
	private boolean isAddedSixth(String chord) {
		return chord.contains("6");
	}

	private boolean isAddedNinth(String chord) {
		return !chord.matches("(?s).*7((/9)|(add(9|2))).*") // 7add9 = 9
				&& (chord.matches("(?s).*(/|add)(9|2).*") || (chord.matches("(?s).*2.*")
						&& !chord.matches("(?s).*[0-9]2.*") && !isSuspendedSecond(chord)));
	}

	private boolean isAddedEleventh(String chord) {
		return !chord.matches("(?s).*(9|2)(/|add)11.*") // 9add11 = 11
				&& (chord.contains("add11") || chord.contains("/11"));
	}

	private boolean isAddedThirteenth(String chord) {
		return !chord.matches("(?s).*11(/|add)13.*") // 11add13 = 13
				&& (chord.contains("add13") || chord.contains("/13"));
	}

	// numbers
	private boolean isPowerChord(String chord) {
		return getSymbols(chord).matches("5|8");
	}

	private boolean isSeventh(String chord) {
		return chord.contains("7") || isHalfDiminished(chord); // C� = C�7
	}

	private boolean isNinth(String chord) {
		return chord.contains("9");
	}

	private boolean isEleventh(String chord) {
		return chord.contains("11");
	}

	private boolean isThirteenth(String chord) {
		return chord.contains("13");
	}

	// flats/diminished
	private boolean isJustDiminishedFifth(String chord) {
		return chord.matches("(?s).*([0-9]+)(dim|m|-|b)5.*") || getSymbols(chord).matches("(?s).*((-|b)5).*");
	}

	private boolean isFlatSixth(String chord) {
		return getSymbols(chord).matches("(?s).*(-|b)6.*");
	}

	private boolean isFlatNinth(String chord) {
		return getSymbols(chord).matches("\\S+(-|b|dim)9.*") || chord.matches("(?s).*add\\(?m2.*")
				|| getSymbols(chord).contains("b9");
	}

	private boolean isDiminishedEleventh(String chord) {
		return getSymbols(chord).matches("\\S+(-|b|dim)11.*") || getSymbols(chord).contains("b11");
	}

	private boolean isDiminishedThirteenth(String chord) {
		return getSymbols(chord).matches("\\S+(-|b|dim)13.*") || getSymbols(chord).contains("b13");
	}

	// sharp/augmented
	private boolean isSharpSixth(String chord) {
		return chord.matches("(?s).*(+|#)6.*");
	}

	private boolean isAugmentedFifth(String chord) {
		return chord.matches("(?s).*([0-9]+)(\\+|#|aug)5.*") || getSymbols(chord).matches("(?s).*(\\+|#)5.*");
	}

	private boolean isSharpNinth(String chord) {
		return getSymbols(chord).matches("\\S+(\\+|#|aug)9.*") || getSymbols(chord).contains("#9");
	}

	private boolean isSharpEleventh(String chord) {
		return getSymbols(chord).matches("\\S+(\\+|#|aug)11.*") || getSymbols(chord).contains("#11");
	}

	private boolean isNoteValid(String note) {
		Pattern notePattern = Pattern.compile("^[CDEFGAB][#b]?$");
		Matcher matcher = notePattern.matcher(note);
		return matcher.matches();
	}

	// ref
	// https://www.scales-chords.com/chord
	// http://jguitar.com/
	// http://scottdavies.net/chords_and_scales/music.html
	// http://members.jamplay.com/teaching-tools/chord-library/family/c?
	// https://chord-c.com/guitar-chord/
	// http://julianrosenblum.com/sharp11-client/
}
