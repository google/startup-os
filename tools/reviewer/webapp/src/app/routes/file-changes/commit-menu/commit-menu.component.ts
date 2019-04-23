import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';

import { DocumentEventService } from '@/core/services';
import { CommitInfo } from './commit-popup';

// DragElement is an orange circle (or square), which can be dragged and dropped by user
// to pick another commit.
// Point is a white circle (or square), where dragElement can be dropped.
interface DragElement {
  x: number;
  offset: number;
  isClicked: boolean;
  index: number;
  isExist: boolean;
  isSelected: boolean;
}

// Orange line, which connects two drag elements
interface Bridge {
  x: number;
  width: number;
}

export interface CommitsParam {
  leftCommitId: string;
  rightCommitId: string;
}

const dragWidth: number = 14;
const distanceBetweenPoints: number = 31;
const magnetField: number = 12;

// Menu to pick commits
// How it looks: https://i.imgur.com/Svk6ok7.png
@Component({
  selector: 'commit-menu',
  templateUrl: './commit-menu.component.html',
  styleUrls: ['./commit-menu.component.scss'],
})
export class CommitMenuComponent implements OnInit, OnDestroy {
  menuOffset: number;
  leftDrag: DragElement;
  rightDrag: DragElement;
  bridge: Bridge = { x: 0, width: 0 };
  commitInfo: CommitInfo = {
    id: '',
    timestamp: 0,
    offset: 0,
    isVisible: false,
    isInit: false,
  };
  mousemoveSubscription = new Subscription();
  mouseupSubscription = new Subscription();

  @Input() leftCommitId: string;
  @Input() rightCommitId: string;
  @Input() commitIdList: string[];
  @Output() changeCommitIdEmitter = new EventEmitter<CommitsParam>();

  constructor(private documentEventService: DocumentEventService) { }

  ngOnInit() {
    this.initDragElements();

    // When user releases left mouse button
    this.mouseupSubscription = this.documentEventService.mouseup.subscribe(() => {
      this.destroyMouseMoveEventHandler();

      // If drag element changed its point, then change commit id of page
      if (this.leftDrag.isClicked) {
        const commitId: string = this.getCommitId(this.leftDrag, this.rightDrag, -1);
        if (this.leftCommitId !== commitId) {
          this.leftCommitId = commitId;
          this.changeCommitIdEmitter.emit({
            leftCommitId: this.leftCommitId,
            rightCommitId: this.rightCommitId,
          });
        }
      }
      if (this.rightDrag.isClicked) {
        const commitId: string = this.getCommitId(this.rightDrag, this.leftDrag, 1);
        if (this.rightCommitId !== commitId) {
          this.rightCommitId = commitId;
          this.changeCommitIdEmitter.emit({
            leftCommitId: this.leftCommitId,
            rightCommitId: this.rightCommitId,
          });
        }
      }
      this.leftDrag.isClicked = false;
      this.rightDrag.isClicked = false;
    });
  }

  // Starts listening mouse moving
  createMouseMoveEventHandler(): void {
    document.onmousemove = (event: MouseEvent) => {
      if (this.leftDrag.isClicked) {
        this.moveDragElement(event.pageX, this.leftDrag, this.rightDrag, -1);
      }
      if (this.rightDrag.isClicked) {
        this.moveDragElement(event.pageX, this.rightDrag, this.leftDrag, 1);
      }
      this.moveBridge();
    };
  }

  // Stops listening mouse moving
  destroyMouseMoveEventHandler(): void {
    document.onmousemove = null;
  }

  // Creates drag elements and bridge on the stage
  initDragElements(): void {
    const leftIndex: number = this.getCommitIndex(this.leftCommitId);
    const rightIndex: number = this.getCommitIndex(this.rightCommitId);
    this.leftDrag = {
      x: leftIndex * distanceBetweenPoints,
      offset: 0,
      isClicked: false,
      index: leftIndex,
      isExist: !!this.leftCommitId,
      isSelected: true,
    };
    this.rightDrag = {
      x: rightIndex * distanceBetweenPoints,
      offset: 0,
      isClicked: false,
      index: rightIndex,
      isExist: rightIndex !== -1,
      isSelected: true,
    };
    this.moveBridge();
  }

  // When user clicks on drag element
  mousedown(event: MouseEvent, dragElement: DragElement): void {
    if (event.button === 0) { // left mouse button
      if (this.menuOffset === undefined) {
        // We gets mouse x on whole screen, but we need x on menu component only
        this.menuOffset = event.pageX - event.layerX - dragElement.x;
      }
      dragElement.offset = event.offsetX;
      dragElement.isClicked = true;
      this.createMouseMoveEventHandler();
    }
  }

  // Moves drag element to nearest point and gets commit id of the point
  getCommitId(
    dragElement: DragElement,
    dragElement2: DragElement,
    step: number,
  ): string {
    let index: number = this.getNearestX(dragElement.x);
    if (index === dragElement2.index && dragElement2.isExist) {
      // If nearest point is already occupied
      index += step;
    }
    dragElement.index = index;
    dragElement.x = distanceBetweenPoints * index;
    this.moveBridge();
    return this.commitIdList[index];
  }

  // Converts mouse X to X of drag element
  getX(mouseX: number, dragElement: DragElement): number {
    let x: number = mouseX - this.menuOffset - dragElement.offset;
    x = Math.max(x, 0);
    x = Math.min(x, (this.commitIdList.length - 1) * distanceBetweenPoints);
    return x;
  }

  // Pulls drag element to nearest point
  magnet(x: number, dragElement: DragElement): void {
    const centerX: number = x + dragWidth / 2;
    const n: number = this.getNearestX(x);
    const pointX: number = dragWidth / 2 + distanceBetweenPoints * n;
    if (centerX > pointX - magnetField / 2 && centerX < pointX + magnetField / 2) {
      dragElement.x = pointX - dragWidth / 2;
      dragElement.isSelected = true;
    } else {
      dragElement.isSelected = false;
      dragElement.x = x;
    }
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
    if (dragElement2.isExist) {
      const secondDragFrameX: number = dragElement2.x + dragWidth * step;
      if (x * step < secondDragFrameX * step) {
        x = secondDragFrameX;
      }
    }

    this.magnet(x, dragElement);
  }

  moveBridge(): void {
    this.bridge.x = this.leftDrag.x + dragWidth / 2;
    this.bridge.width = this.rightDrag.x - this.leftDrag.x;
  }

  setPopupData(commitIndex: number): void {
    this.commitInfo.id = this.commitIdList[commitIndex];
    // TODO: Display real time of a commit
    this.commitInfo.timestamp = 1543122301000;
    this.commitInfo.offset = commitIndex * distanceBetweenPoints;
    this.commitInfo.isInit = true;
    this.commitInfo.isVisible = true;
  }

  showPopup(): void {
    if (this.commitInfo.isInit) {
      this.commitInfo.isVisible = true;
    }
  }

  isDotSelected(index: number): boolean {
    const isLeft: boolean = this.leftDrag.x === index * distanceBetweenPoints;
    const isRight: boolean = this.rightDrag.x === index * distanceBetweenPoints;
    return isLeft || isRight;
  }

  getCommitIndex(commitId: string): number {
    return this.commitIdList.indexOf(commitId);
  }

  ngOnDestroy() {
    this.destroyMouseMoveEventHandler();
    this.mouseupSubscription.unsubscribe();
  }
}
