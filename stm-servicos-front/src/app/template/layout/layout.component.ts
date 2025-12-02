import { Component, OnInit } from '@angular/core';
import { LayoutProps } from './layoutprops';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, map } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent implements OnInit {

  props: LayoutProps = { titulo: '', subTitulo: '' };

  constructor(private router: Router, private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.router.events
      .pipe(
        map(() => this.obterPropriedadesLayout()),
        filter((props): props is LayoutProps => !!props)
      ).subscribe(props => {
        this.props = props;
      });
  }

  obterPropriedadesLayout(): LayoutProps | null {
    let rotaFilha = this.activatedRoute.firstChild;

    while (rotaFilha?.firstChild) {
      rotaFilha = rotaFilha.firstChild;
    }

    const data = rotaFilha?.snapshot.data;

    if (data && 'titulo' in data && 'subTitulo' in data) {
      return data as LayoutProps;
    }

    return null;
  }
}
