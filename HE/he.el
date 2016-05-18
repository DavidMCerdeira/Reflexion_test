
compile CI

language C{
	"*.c"
	"*.h"
}

interface Ia {
	Fa
	Fb
}

component CA (C) {
	properties:
		int Pa: 2
		int Pe: 3
	references:
		Ia Ra
}

component CE (C){
	properties:
		float Pa: 2.0
	services:
		Ia Sa
}

component CI (C){
	subcomponents:
		CA SCa
		CE SCe
	bind SCa.Ra to SCe.Sa
}
