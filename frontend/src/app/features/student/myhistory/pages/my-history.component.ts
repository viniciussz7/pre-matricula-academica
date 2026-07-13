import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-my-history',
  standalone: true,
  imports: [RouterLink],
  templateUrl: `./my-history.html`
})
export class MyHistoryComponent {}