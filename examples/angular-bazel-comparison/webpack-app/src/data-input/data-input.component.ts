import { Component, OnInit } from '@angular/core';

import { MockService } from '../services/mock.service';

@Component({
  selector: 'data-input',
  templateUrl: './data-input.component.html',
  styleUrls: ['./data-input.component.scss']
})
export class DataInputComponent implements OnInit {
  inputData: string = '';

  constructor(private mockService: MockService) { }

  ngOnInit() {
    this.clear();
  }

  save(): void {
    this.mockService.setData(this.inputData);
  }

  clear(): void {
    this.inputData = this.mockService.getData();
  }
}
