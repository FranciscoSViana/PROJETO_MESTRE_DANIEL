import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const token = auth.getToken();

    if (!token) {
        router.navigate(['']);
        return false;
    }

    // Verifica se o token ainda é válido
    try {
        const payload: any = JSON.parse(atob(token.split('.')[1]));
        const agora = Math.floor(Date.now() / 1000);

        if (payload.exp && payload.exp > agora) {
            return true; // Token válido, pode passar
        }
    } catch {
        router.navigate(['']);
        return false;
    }

    // Token expirado — tenta renovar antes de bloquear
    const refreshToken = auth.getRefreshToken();
    if (!refreshToken) {
        router.navigate(['']);
        return false;
    }

    return auth.renovarToken().pipe(
        map(() => true),
        catchError(() => {
            auth.logout();
            return of(false);
        })
    );
};