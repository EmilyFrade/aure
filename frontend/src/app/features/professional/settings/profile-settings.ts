import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProfessionalProfile } from '../../../core/models/api.models';
import { ProfessionalService } from '../../../core/services/professional.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Avatar } from '../../../shared/ui/avatar';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { TextField } from '../../../shared/ui/text-field';
import { BR_UFS } from '../../../shared/utils/br-states';
import { apiErrorMessage } from '../../../shared/utils/format';
import { resizeImage } from '../../../shared/utils/image';
import { digitsOnly, maskPhoneBR } from '../../../shared/utils/masks';

@Component({
  selector: 'app-profile-settings',
  imports: [FormsModule, Avatar, Button, Card, Icon, Spinner, TextField],
  templateUrl: './profile-settings.html'
})
export class ProfileSettings {
  private readonly professionals = inject(ProfessionalService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();
  readonly saved = output<ProfessionalProfile>();

  protected readonly ufs = BR_UFS;
  protected readonly maskPhone = maskPhoneBR;

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly uploading = signal(false);
  protected readonly photoUrl = signal<string | null>(null);

  protected readonly name = signal('');
  protected readonly bio = signal('');
  protected readonly phone = signal('');
  protected readonly brandName = signal('');
  protected readonly address = signal('');
  protected readonly city = signal('');
  protected readonly state = signal('');

  protected readonly phoneError = computed(() => {
    const digits = digitsOnly(this.phone());
    return digits.length > 0 && digits.length < 10 ? 'Informe o DDD e o número.' : null;
  });

  protected readonly canSave = computed(
    () =>
      !!this.name().trim() &&
      !!this.city().trim() &&
      !!this.state() &&
      !this.phoneError()
  );

  ngOnInit(): void {
    this.professionals.getProfile(this.professionalId()).subscribe({
      next: (profile) => {
        this.fill(profile);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected save(): void {
    if (!this.canSave()) return;
    this.saving.set(true);
    this.professionals
      .updateProfile(this.professionalId(), {
        name: this.name().trim(),
        bio: this.bio().trim() || undefined,
        phone: digitsOnly(this.phone()) || undefined,
        brand_name: this.brandName().trim() || undefined,
        address: this.address().trim() || undefined,
        city: this.city().trim(),
        state: this.state()
      })
      .subscribe({
        next: (profile) => {
          this.fill(profile);
          this.saving.set(false);
          this.saved.emit(profile);
          this.toast.success('Perfil atualizado.');
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          if (err.status > 0 && err.status < 500) {
            this.toast.error(apiErrorMessage(err, 'Não foi possível salvar o perfil.'));
          }
        }
      });
  }

  protected async choosePhoto(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;

    this.uploading.set(true);
    let photo: Blob;
    try {
      photo = await resizeImage(file);
    } catch {
      this.uploading.set(false);
      this.toast.error('Não foi possível abrir essa imagem. Tente uma foto JPG ou PNG.');
      return;
    }

    this.professionals.uploadPhoto(this.professionalId(), photo).subscribe({
      next: (profile) => this.photoChanged(profile, 'Foto atualizada.'),
      error: (err: HttpErrorResponse) => this.photoFailed(err)
    });
  }

  protected removePhoto(): void {
    this.uploading.set(true);
    this.professionals.deletePhoto(this.professionalId()).subscribe({
      next: (profile) => this.photoChanged(profile, 'Foto removida.'),
      error: (err: HttpErrorResponse) => this.photoFailed(err)
    });
  }

  private photoChanged(profile: ProfessionalProfile, message: string): void {
    this.photoUrl.set(profile.photo_url);
    this.uploading.set(false);
    this.toast.success(message);
  }

  private photoFailed(err: HttpErrorResponse): void {
    this.uploading.set(false);
    if (err.status > 0 && err.status < 500) {
      this.toast.error(apiErrorMessage(err, 'Não foi possível atualizar a foto.'));
    }
  }

  private fill(profile: ProfessionalProfile): void {
    this.photoUrl.set(profile.photo_url);
    this.name.set(profile.name);
    this.bio.set(profile.bio ?? '');
    this.phone.set(maskPhoneBR(profile.phone ?? ''));
    this.brandName.set(profile.brand_name === profile.name ? '' : profile.brand_name);
    this.address.set(profile.address ?? '');
    this.city.set(profile.city ?? '');
    this.state.set(profile.state ?? '');
  }
}
