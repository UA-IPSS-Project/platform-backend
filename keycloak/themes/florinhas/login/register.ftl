<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
        Crie a sua conta
    <#elseif section = "form">
        <form id="kc-register-form" class="space-y-4" action="${url.registrationAction}" method="post">
            
            <div class="space-y-2">
                <label for="firstName" class="text-sm font-medium leading-none">Nome Completo</label>
                <input type="text" id="firstName" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="firstName" value="${(register.formData.firstName!'')}" autocomplete="given-name" placeholder="John Doe" />
            </div>

            <!-- Fake Last Name as it is required by KK but user only types full name -->
            <input type="hidden" id="lastName" name="lastName" value="-" />

            <div class="space-y-2">
                <label for="user.attributes.nif" class="text-sm font-medium leading-none">NIF</label>
                <input type="text" id="user.attributes.nif" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="user.attributes.nif" value="${(register.formData['user.attributes.nif']!'')}" placeholder="123456789" />
            </div>

            <div class="space-y-2">
                <label for="user.attributes.contact" class="text-sm font-medium leading-none">Contacto Telefónico (Opcional)</label>
                <input type="text" id="user.attributes.contact" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="user.attributes.contact" value="${(register.formData['user.attributes.contact']!'')}" placeholder="910000000" />
            </div>

            <div class="space-y-2">
                <label for="email" class="text-sm font-medium leading-none">E-mail</label>
                <input type="email" id="email" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="email" value="${(register.formData.email!'')}" autocomplete="email" placeholder="nome@exemplo.com" />
            </div>

            <div class="space-y-2">
                <label for="user.attributes.birthDate" class="text-sm font-medium leading-none">Data de Nascimento (DD/MM/YYYY)</label>
                <input type="text" id="user.attributes.birthDate" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="user.attributes.birthDate" value="${(register.formData['user.attributes.birthDate']!'')}" placeholder="DD/MM/YYYY"/>
            </div>

            <#if !realm.registrationEmailAsUsername>
                <div class="space-y-2">
                    <label for="username" class="text-sm font-medium leading-none">Nome de Utilizador</label>
                    <input type="text" id="username" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="username" value="${(register.formData.username!'')}" autocomplete="username" />
                </div>
            </#if>

            <#if passwordRequired??>
                <div class="space-y-2">
                    <label for="password" class="text-sm font-medium leading-none">Palavra-passe</label>
                    <input type="password" id="password" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="password" autocomplete="new-password"/>
                </div>

                <div class="space-y-2">
                    <label for="password-confirm" class="text-sm font-medium leading-none">Confirmar Palavra-passe</label>
                    <input type="password" id="password-confirm" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="password-confirm" />
                </div>
            </#if>

            <div class="flex items-center space-x-2 py-4">
                <input type="checkbox" id="terms" required class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary shadow-sm" />
                <label for="terms" class="text-sm font-medium leading-5">Confirmo que tenho mais de 18 anos e aceito os Termos de Serviço.</label>
            </div>

            <div class="space-y-2 pt-2">
                <button type="submit" class="inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 w-full">
                    Aderir
                </button>
            </div>
            
            <div class="text-center text-sm text-muted-foreground mt-4">
                Já tem uma conta? <a href="${url.loginUrl}" class="text-primary hover:text-primary/90 hover:underline font-medium">Entrar</a>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
