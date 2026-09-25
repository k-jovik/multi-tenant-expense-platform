import { useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import api from '@/lib/api';
import type { AuthResponse } from '@/types/api'
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from '@/components/ui/card';

interface LoginFormData {
    tenantName: string;
    email: string;
    password: string;
}

export default function LoginPage() {

    const { login } = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState<string>('');
    const {
        register,
        handleSubmit,
        formState: { isSubmitting },
    } = useForm<LoginFormData>();

    const onSubmit = async (data: LoginFormData) => {
        setError('')
        try {
            const response = await api.post<AuthResponse>('/auth/login', data);
            login(response.data);
            navigate('/');
        } catch {
            setError('Invalid credentials. Please try again.');
        }
    }
    return (
        <div className="flex justify-center items-center min-h-screen bg-background">
            <Card className='w-full max-w-md'>
                <CardHeader>
                    <CardTitle>Sign in</CardTitle>
                    <CardDescription>Sign in to your expense account</CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <CardContent className='space-y-4 mb-4'>
                        <div className='space-y-2'>
                            <Label htmlFor='tenantName'>
                                Company
                            </Label>
                            <Input
                                id='tenantName'
                                {...register('tenantName', { required: true })}
                                placeholder='Acme Corp'
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="email">Email</Label>
                            <Input
                                id="email"
                                type="email"
                                {...register('email', { required: true })}
                                placeholder="you@company.com"
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                type="password"
                                {...register('password', { required: true })}
                            />
                        </div>
                        {error && (
                            <p className="text-sm text-destructive">{error}</p>
                        )}
                    </CardContent>
                    <CardFooter className="flex flex-col gap-4">
                        <Button type="submit" className="w-full" disabled={isSubmitting}>
                            {isSubmitting ? 'Signing in...' : 'Sign in'}
                        </Button>
                        <p className="text-sm text-muted-foreground">
                            Don't have an account?{' '}
                            <Link to="/register" className="text-primary underline">
                                Sign up
                            </Link>
                        </p>
                    </CardFooter>
                </form>
            </Card>
        </div>
    )
}