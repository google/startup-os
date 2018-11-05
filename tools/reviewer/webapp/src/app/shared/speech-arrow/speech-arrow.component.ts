import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

const svgNS: string = 'http://www.w3.org/2000/svg';

// An arrow of speech bubble
// How it looks: https://i.imgur.com/bQDdg4B.png
@Component({
  selector: 'speech-arrow',
  templateUrl: './speech-arrow.component.html',
})
export class SpeechArrowComponent implements OnInit {
  width: number;
  height: number;

  @Input() size: number;
  @Input() border: number;
  @Input() color: string;
  @Input() bordercolor: string;
  // [dx, dy, blur, opacity]
  @Input() shadow: number[];

  @ViewChild('svg') svg: ElementRef;

  ngOnInit() {
    const svgElement: SVGSVGElement = this.svg.nativeElement;
    this.width = this.size + this.border * 2 + this.shadow[0] + this.shadow[2];
    this.height = this.size + 1; // +1 to add bottom border
    svgElement.setAttributeNS(null, 'width', this.width.toString());
    svgElement.setAttributeNS(null, 'height', this.height.toString());

    svgElement.appendChild(this.createFilter());
    svgElement.appendChild(this.createPath());
    svgElement.appendChild(this.createPolyline());
    svgElement.appendChild(this.createRect());
  }

  // Create triangle, which is base of the arrow
  createPath(): Element {
    const path: Element = document.createElementNS(svgNS, 'path');
    const coords: number[][] = [
      [this.border, this.size + 1],
      [this.size / 2 + this.border, this.border],
      [this.size + this.border, this.size + 1],
    ];
    const d: string = 'M' + coords.map(coord => coord.join(',')).join(' L') + 'Z';
    path.setAttributeNS(null, 'd', d);
    path.setAttributeNS(null, 'fill', this.color);
    path.setAttributeNS(null, 'filter', 'url(#shadow)');

    return path;
  }

  // Create border
  createPolyline(): Element {
    const polyline: Element = document.createElementNS(svgNS, 'polyline');
    const polylinecoords: number[][] = [
      [0, this.size - this.border / 2],
      [this.border, this.size - this.border / 2],
      [this.size / 2 + this.border, this.border],
      [this.size + this.border, this.size - this.border / 2],
      [this.width, this.size - this.border / 2],
    ];
    const points: string = polylinecoords.map(coord => coord.join(',')).join(' ');
    polyline.setAttributeNS(null, 'points', points);
    polyline.setAttributeNS(null, 'fill', 'none');
    polyline.setAttributeNS(null, 'stroke', this.bordercolor);
    polyline.setAttributeNS(null, 'stroke-width', this.border.toString());

    return polyline;
  }

  // Create bottom border with color of background
  createRect(): Element {
    const rect: Element = document.createElementNS(svgNS, 'rect');
    rect.setAttributeNS(null, 'x', '0');
    rect.setAttributeNS(null, 'y', (this.height - 1).toString());
    rect.setAttributeNS(null, 'width', this.width.toString());
    rect.setAttributeNS(null, 'height', '1');
    rect.setAttributeNS(null, 'fill', this.color);

    return rect;
  }

  // Create shadow
  createFilter(): Element {
    const filter: Element = document.createElementNS(svgNS, 'filter');
    filter.setAttributeNS(null, 'id', 'shadow');
    filter.setAttributeNS(null, 'x', '0');
    filter.setAttributeNS(null, 'y', '0');
    filter.setAttributeNS(null, 'width', '200%');
    filter.setAttributeNS(null, 'height', '200%');

    const feOffset: Element = document.createElementNS(svgNS, 'feOffset');
    feOffset.setAttributeNS(null, 'result', 'offOut');
    feOffset.setAttributeNS(null, 'in', 'SourceGraphic');
    feOffset.setAttributeNS(null, 'dx', this.shadow[0].toString());
    feOffset.setAttributeNS(null, 'dy', this.shadow[1].toString());

    const feGaussianBlur: Element = document.createElementNS(svgNS, 'feGaussianBlur');
    feGaussianBlur.setAttributeNS(null, 'result', 'blurOut');
    feGaussianBlur.setAttributeNS(null, 'in', 'matrixOut');
    feGaussianBlur.setAttributeNS(null, 'stdDeviation', this.shadow[2].toString());

    const feBlend: Element = document.createElementNS(svgNS, 'feBlend');
    feBlend.setAttributeNS(null, 'in', 'SourceGraphic');
    feBlend.setAttributeNS(null, 'in2', 'blurOut');
    feBlend.setAttributeNS(null, 'mode', 'normal');

    const value: number = 1 - this.shadow[3];
    const feColorMatrix: Element = document.createElementNS(svgNS, 'feColorMatrix');
    feColorMatrix.setAttributeNS(null, 'result', 'matrixOut');
    feColorMatrix.setAttributeNS(null, 'in', 'offOut');
    feColorMatrix.setAttributeNS(null, 'type', 'matrix');
    feColorMatrix.setAttributeNS(null, 'values',
      `${value} 0        0        0        0
       0        ${value} 0        0        0
       0        0        ${value} 0        0
       0        0        0        1        0`,
    );

    filter.appendChild(feOffset);
    filter.appendChild(feColorMatrix);
    filter.appendChild(feGaussianBlur);
    filter.appendChild(feBlend);

    return filter;
  }
}
