//iload test_i.coe 0
//set_reg 9 8191
//set_dmem 6 11
//put_channel 2 20
//dump_channel 3

.text
	sub $s1,$s1 //test sub
	sub $s2,$s2 //$s1,$s2 = 0
hello:
	addi $s1,17 //$s1=0x11 = 17(d)
	sloi $s2,0x14 //$s2=0x14 = 20(d) 
	move $s1,1($s2) //$s1=$s2 + 1 = 0x15 =21(d)
	sw $s1, 0($s2)  //dmem[20] = $s1 = 0x15
	nor $s1,$s2  //$s1 = 0x3ffffffea
	move $t1,2($zero) //$t1 = $zero + 2 = 2
	in $t0,$t1  //$t0 = input from channel 2 = 20
	jal bye    //jump to bye, set $ra = PC = 10
	halt
boy:
	srl $s2,2 //$s2= 101b = 5(d)  
	addi $t1,1 //$t1 = $t1 + 1 = 3
	out $s2, $t1 //ouput $s2 = 5 to channel 3
	blt 2(world)  //if $bcl < $bcr, jump world, else $bcr = $bcr + 2 and PC++
	lw $s3,1($s2) //$s3 = dmem[5+1] = 11(d) = 0xb
	be 1(hello)  // if $bcl(=0) == $bcr(=2), jump hello, else PC++
	jmp world   //jump to world
bye:
	addi $sp,-1 // $sp = $sp - 1
	sw $ra,0($sp) //store $ra to stack for return
	la boy //$la = addr of 'boy' = 0xb
	jr $la //jump to addr of content of $la, which is 'boy'
world:
	move $v0,2($s3) // return value $v0(reg No.2) = $s3 + 2 = 13(d) = 0xd
	lw $ra 0($sp)
	addi $sp,1
	jr $ra  //return from 'jal bye'
	
	