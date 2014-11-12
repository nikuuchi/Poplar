int main()
{
	int a = 0;
	a += 1;
	a *= 1;
	a /= 1;
	a <<= 1;
	a >>= 1;
	a |= 1;
	a &= 1;
	printf("hoge%d", a);
	printf("%d", sizeof(a));
	return 0;
}
