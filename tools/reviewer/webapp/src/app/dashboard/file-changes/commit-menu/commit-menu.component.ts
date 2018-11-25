import { Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { MouseService } from '@/core/services';
import { LoadService, StateService } from '../services';
import { CommitInfo } from './commit-popup';

// Drag element is an orange circle (or square), which can be drag and droped by user
// to pick another commit.
// Point is an white circle (or square), where can be dropped drag element.
interface DragElement {
  x: number;
  offset: number;
  isClicked: boolean;
  index: number;
}

// Orange line, which connects two drag elements
interface Bridge {
  x: number;
  width: number;
}

const dragWidth: number = 20;
const distanceBetweenPoints: number = 40;
const magnetField: number = 20;

// Menu to pick commits
// How it looks: https://i.imgur.com/Svk6ok7.png
@Component({
  selector: 'commit-menu',
  templateUrl: './commit-menu.component.html',
  styleUrls: ['./commit-menu.component.scss'],
})
export class CommitMenuComponent implements OnDestroy {
  menuOffset: number;
  leftDrag: DragElement;
  rightDrag: DragElement;
  bridge: Bridge = { x: 0, width: 0 };
  commitInfo: CommitInfo = {
    id: '',
    timestamp: 0,
    offset: 0,
    isVisible: false,
  };
  mousemoveSubscription = new Subscription();
  mouseupSubscription = new Subscription();

  constructor(
    private mouseService: MouseService,
    private stateService: StateService,
    private loadService: LoadService,
  ) {
    this.initDragElements();

    // When mouse cursor is moved
    this.mousemoveSubscription = this.mouseService.mousemove.subscribe(event => {
      if (this.leftDrag.isClicked) {
        this.moveDragElement(event.pageX, this.leftDrag, this.rightDrag, -1);
      }
      if (this.rightDrag.isClicked) {
        this.moveDragElement(event.pageX, this.rightDrag, this.leftDrag, 1);
      }
      this.moveBridge();
    });

    // When user clicks up (drops) left mouse button
    this.mouseupSubscription = this.mouseService.mouseup.subscribe(() => {
      // If drag element changed its point, then change commit id of page
      if (this.leftDrag.isClicked) {
        const commitId: string = this.getCommitId(this.leftDrag, this.rightDrag, -1);
        if (this.stateService.leftCommitId !== commitId) {
          this.stateService.leftCommitId = commitId;
          this.loadService.changeCommitId();
        }
      }
      if (this.rightDrag.isClicked) {
        const commitId: string = this.getCommitId(this.rightDrag, this.leftDrag, 1);
        if (this.stateService.rightCommitId !== commitId) {
          this.stateService.rightCommitId = commitId;
          this.loadService.changeCommitId();
        }
      }
      this.leftDrag.isClicked = false;
      this.rightDrag.isClicked = false;
    });
  }

  // Creates drag elements and bridge on the stage
  initDragElements(): void {
    let leftIndex: number = this.getCommitIndex(this.stateService.leftCommitId);
    let rightIndex: number = this.getCommitIndex(this.stateService.rightCommitId);
    if (leftIndex === -1 || rightIndex === -1 || leftIndex >= rightIndex) {
      // If commits not found, pick first and last commits
      leftIndex = 0;
      rightIndex = this.stateService.commitIdList.length - 1;
      this.stateService.leftCommitId = this.stateService.commitIdList[leftIndex];
      this.stateService.rightCommitId = this.stateService.commitIdList[rightIndex];
      this.loadService.changeCommitId();
    }
    this.leftDrag = {
      x: leftIndex * distanceBetweenPoints,
      offset: 0,
      isClicked: false,
      index: leftIndex,
    };
    this.rightDrag = {
      x: rightIndex * distanceBetweenPoints,
      offset: 0,
      isClicked: false,
      index: rightIndex,
    };
    this.moveBridge();
  }

  // When user clicks on drag element
  mousedown(event: MouseEvent, dragElement: DragElement): void {
    if (event.which === 1) { // left mouse button
      if (this.menuOffset === undefined) {
        // We gets mouse x on whole screen, but we need x on menu component only
        this.menuOffset = event.pageX - event.layerX - dragElement.x;
      }
      dragElement.offset = event.offsetX;
      dragElement.isClicked = true;
    }
  }

  // Moves drag element to nearest point and gets commit id of the point
  getCommitId(
    dragElement: DragElement,
    dragElement2: DragElement,
    step: number,
  ): string {
    let index: number = this.getNearestX(dragElement.x);
    if (index === dragElement2.index) {
      // If nearest point is already occupied
      index += step;
    }
    dragElement.index = index;
    dragElement.x = distanceBetweenPoints * index;
    this.moveBridge();
    return this.stateService.commitIdList[index];
  }

  // Converts mouse X to X of drag element
  getX(mouseX: number, dragElement: DragElement): number {
    let x: number = mouseX - this.menuOffset - dragElement.offset;
    x = Math.max(x, 0);
    x = Math.min(x, (this.stateService.commitIdList.length - 1) * distanceBetweenPoints);
    return x;
  }

  // Pulls drag element to nearest point
  getMagnetX(x: number): number {
    const centerX: number = x + dragWidth / 2;
    const n: number = this.getNearestX(x);
    const pointX: number = dragWidth / 2 + distanceBetweenPoints * n;
    if (centerX > pointX - magnetField / 2 && centerX < pointX + magnetField / 2) {
      return pointX - dragWidth / 2;
    }
    return x;
  }

  // Gets X of nearest point to the x
  getNearestX(x: number): number {
    return Math.round(x / distanceBetweenPoints);
  }

  moveDragElement(
    mouseX: number,
    dragElement: DragElement,
    dragElement2: DragElement,
    step: number,
  ): void {
    let x: number = this.getX(mouseX, dragElement);

    // Stop drag element by second drag element
    const secondDragFrameX: number = dragElement2.x + dragWidth * step;
    if (x * step < secondDragFrameX * step) {
      x = secondDragFrameX;
    }

    dragElement.x = this.getMagnetX(x);
  }

  moveBridge(): void {
    this.bridge.x = this.leftDrag.x + dragWidth / 2;
    this.bridge.width = this.rightDrag.x - this.leftDrag.x;
  }

  getCommitIndex(commitId: string): number {
    return this.stateService.commitIdList.indexOf(commitId);
  }

  showPopup(commitIndex: number): void {
    this.commitInfo.id = this.stateService.commitIdList[commitIndex];
    // TODO: Display real time of a commit
    this.commitInfo.timestamp = 1543122301000;
    this.commitInfo.offset = commitIndex * distanceBetweenPoints;
    this.commitInfo.isVisible = true;
  }

  hidePopup(): void {
    this.commitInfo.isVisible = false;
  }

  ngOnDestroy() {
    this.mousemoveSubscription.unsubscribe();
    this.mouseupSubscription.unsubscribe();
  }
}
