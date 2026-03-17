import {
    HttpErrorResponse, HttpEvent, HttpHandler,
    HttpInterceptor, HttpRequest
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { AuthService } from "./auth.service";
import {
    catchError, Observable, switchMap, throwError, BehaviorSubject, filter, take
} from "rxjs";

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

    private renovando = false;
    private renovacaoSubject = new BehaviorSubject<string | null>(null);

    constructor(private auth: AuthService) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
            return next.handle(req);
        }

        const token = this.auth.getToken();
        if (token) {
            req = this.adicionarToken(req, token);
        }

        return next.handle(req).pipe(
            catchError((err: HttpErrorResponse) => {
                if (err.status !== 401) return throwError(() => err);

                if (this.renovando) {
                    // Aguarda a renovação em andamento e repete a requisição
                    return this.renovacaoSubject.pipe(
                        filter(t => t !== null),
                        take(1),
                        switchMap(novoToken => next.handle(this.adicionarToken(req, novoToken!)))
                    );
                }

                this.renovando = true;
                this.renovacaoSubject.next(null);

                return this.auth.renovarToken().pipe(
                    switchMap(() => {
                        const novoToken = this.auth.getToken()!;
                        this.renovando = false;
                        this.renovacaoSubject.next(novoToken);
                        return next.handle(this.adicionarToken(req, novoToken));
                    }),
                    catchError(erroRenovacao => {
                        this.renovando = false;
                        this.auth.logout(); // Só desloga se o refresh também falhar
                        return throwError(() => erroRenovacao);
                    })
                );
            })
        );
    }

    private adicionarToken(req: HttpRequest<any>, token: string) {
        return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
}