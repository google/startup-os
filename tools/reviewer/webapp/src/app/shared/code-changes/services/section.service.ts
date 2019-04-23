import { Injectable } from '@angular/core';

import { ChangeType, DiffLine } from '@/core/proto';
import { Section } from '../code-changes.interface';

export const linesAroundChange: number = 5;

// Functions related to sections
@Injectable()
export class SectionService {
  // Finds changes in diffLines and creates code groups around it.
  // How it's impelented:
  // Finds a change, remembers its line number.
  // Moves to next change until it's located more than 10 lines from previos one,
  // or it's end of the file.
  // Remembers line number of the change too.
  // Adds section with the two line numbers.
  // Repeat (or end)
  getSections(diffLines: DiffLine[], amountOfLines: number): Section[] {
    let isGroupFound: boolean = true;
    const sections: Section[] = [];
    let sectionArray: number[] = [];

    // Leave only diffLines with changes
    const changeDiffLines: DiffLine[] = diffLines.filter((diffLine: DiffLine) => (
      diffLine.getType() === ChangeType.DELETE ||
      diffLine.getType() === ChangeType.ADD ||
      diffLine.getType() === ChangeType.LINE_PLACEHOLDER
    ));

    // Adds section if next change not found (end of the file) or
    // next change is located too far away.
    function addSection(lineNumber: number, nextDiffLine: DiffLine): void {
      if (
        nextDiffLine === undefined ||
        nextDiffLine.getDiffLineNumber() - lineNumber > linesAroundChange * 2
      ) {
        sectionArray.push(lineNumber + 1);
        const startLineNumber: number = sectionArray[0] - linesAroundChange;
        const endLineNumber: number = sectionArray[1] + linesAroundChange;
        sections.push({
          startLineNumber: Math.max(startLineNumber, 0),
          endLineNumber: Math.min(endLineNumber, amountOfLines),
        });
        sectionArray = [];
        isGroupFound = true;
      }
    }

    // Find sections
    changeDiffLines.forEach((diffLine: DiffLine, index: number) => {
      const lineNumber: number = diffLine.getDiffLineNumber();
      const nextDiffLine: DiffLine = changeDiffLines[index + 1];

      if (isGroupFound) {
        sectionArray.push(lineNumber);
        isGroupFound = false;
        addSection(lineNumber, nextDiffLine);
      } else {
        addSection(lineNumber, nextDiffLine);
      }
    });

    return sections;
  }

  // Merges two sections, if their lines overlap.
  getMergedSections(sections: Section[]): Section[] {
    let isIntersection: boolean = false;

    const newSections: Section[] = [];
    for (let i = 0; i < sections.length; i++) {
      const section: Section = sections[i];
      const nextSection: Section = sections[i + 1];
      let newSection: Section = section;
      if (nextSection) {
        // Merge sections, if second line number of first section is bigger than
        // first line number of second section
        if (section.endLineNumber >= nextSection.startLineNumber) {
          newSection = {
            startLineNumber: section.startLineNumber,
            endLineNumber: nextSection.endLineNumber,
          };
          isIntersection = true;
          i++;
        }
      }
      newSections.push(newSection);
    }

    if (isIntersection) {
      sections = newSections;
    }

    return sections;
  }

  // Gets index of group by line number
  getGroupIndex(lineNumber: number, sections: Section[]): number {
    for (const groupIndex in sections) {
      const startLineNumber: number = sections[groupIndex].startLineNumber;
      const endLineNumber: number = sections[groupIndex].endLineNumber;
      if (lineNumber >= startLineNumber && lineNumber <= endLineNumber) {
        return +groupIndex;
      }
    }
  }
}
